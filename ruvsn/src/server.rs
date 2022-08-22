#[macro_use]
use std::collections::HashMap;
use std::sync::Mutex;
use lazy_static::lazy_static;

use crate::configuration::*;
use crate::protocols::tcp::start;
use crate::protocols::tcp::Connection;

use std::thread;

lazy_static! {
    pub static ref HASHMAP: Mutex<HashMap<u16, Connection>> = {
        let mut m = HashMap::new();
        Mutex::new(m)
    };
}

pub fn init_server() {
    let mode = ServerMode {
        protocol: PROTOCOL,
        encryption: ENCRYPTION,
        lhost: String::from(LHOST),
        lport: LPORT,
        srvhost: String::from(SRVHOST),
        srvport: SRVPORT
    };

    write_initial_connection_file(&mode);



    match &mode.protocol {
        CommunicationMode::Tcp => {
            use crate::protocols::tcp;
            thread::spawn(|| {
                start();
            });
        },
        CommunicationMode::Http => {

        }
        CommunicationMode::Udp => {

        },
    }
}

fn write_initial_connection_file(mode: &ServerMode) {
    use crate::configuration::*;
    use crate::updater::write_initial;

    //Write the initial output
    {
        let mut initial_output = "INITIAL;".to_owned() + &mode.lhost + ";" + &mode.lport.to_string();

        match &mode.protocol {
            CommunicationMode::Tcp => {
                initial_output += &*String::from(";TCP");
            },
            CommunicationMode::Udp => {
                initial_output += &*String::from(";UDP");
            },
            CommunicationMode::Http => {
                initial_output += &*String::from(";HTTP");
            }
        }

        match &mode.encryption {
            EncryptionMode::None => {

            },
            EncryptionMode::Aes(_) => {
                println!("Encryption: AES.")
            },
            EncryptionMode::Rsa(_) => {
                println!("Encryption: RSA.")
            },
        }

        write_initial(&initial_output);
    }
}
