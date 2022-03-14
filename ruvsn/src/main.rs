use std::sync::mpsc;
use std::thread;
use std::time::Duration;
use std::net::{TcpStream};
use std::io::{Read, Write};
use std::str::from_utf8;
use aes::Aes128;
use block_modes::{BlockMode, Cbc};
use block_modes::block_padding::Pkcs7;
//use hex_literal::hex;
use std::str;

type Aes128Cbc = Cbc<Aes128, Pkcs7>;

fn main2() {
    //Create a channel so we can communicate out of the thread
    let (tx, rx) = mpsc::channel();

    //Let's create a thread and send a message from it to the main thread
    thread::spawn(move || {
        //Here we create a vector of Strings
        let vals = vec![
            String::from("hi"),
            String::from("from"),
            String::from("the"),
            String::from("thread"),
        ];

        //Iterate over the vector 
        for val in vals {
            //and send each value to the main thread
            tx.send(val).unwrap();
            //Wait for a second
            thread::sleep(Duration::from_secs(1));
        }
    });

    //Now we'll receive the values from the thread
    for received in rx {
        //And print them.
        println!("Got: {}", received);
    }
}


//I hope you've learned something so far my friend.
//if you haven't, you can find a lot of it here: (lol)

fn main() {
    //Connect to a TCP server through a TCP stream
    match TcpStream::connect("192.168.62.131:1331") {
        //When "Ok" -> connected, then we can read and write to the stream and create a mutable reference to the stream
        Ok(mut stream) => {
            println!("Successfully connected to server in port 1331");
            let mut msg = "74384-432-4-4234-234: just arrived to vacation on: Linux";
            //call httpRequest() and save to string
            /*
            let mut response = httpRequest("htto://127.0.0.1:8082/initialRSA.txt");
            //split response at semicolons into 6 strings
            let mut splitResponse = response.split(";");
            //create a array of 6 strings
            let mut splitResponseArray: [String; 6] = [String::new(), String::new(), String::new(), String::new(), String::new(), String::new()];
            //iterate over the array and assign each string to the array
            for i in 0..6 {
                splitResponseArray[i] = splitResponse.next().unwrap().to_string();
            }
            //let key be 
            //Todo: change key to pass and salt, iv hardcoded, congrats.
            */
            
            /*
            
            let encrypted = aesEncrypt(msg: &str, key: &str, iv: &str);
            //convert encrypted to &[u8]
            let encrypted = encrypted.as_bytes();

            //send it to the server
            stream.write(encrypted).unwrap();

            //Now we'll read the response from the server
            let mut buffer = [0; 1024];
            stream.read(&mut buffer).unwrap();

            //And decrypt it
            //let decrypted = aesDecrypt(&buffer);
            

            //And print it
            println!("Got: {}", from_utf8(&decrypted).unwrap());

            */
        
        },
        Err(e) => {
            println!("Failed to connect: {}", e);
        }
    }
    println!("Terminated.");
}

fn aesEncrypt(msg: &str, key: &str, iv: &str) -> String {
    
    //decode iv and key from base64 to &[u8]
    let iv = base64::decode(iv).unwrap();
    let key = base64::decode(key).unwrap();
    
    
    //convert msg to base64 string
    let msg = base64::encode(msg);

    let mut message = String::from(msg);
    let mut mykey = key;
    
    
    let plaintext=message.as_bytes();
    let key = hex::decode(mykey).expect("Decoding failed");
    
    let cipher = Aes128Cbc::new_from_slices(&key, &iv).unwrap();
    
    let pos = plaintext.len();
    
    let mut buffer = [0u8; 128];
    
    buffer[..pos].copy_from_slice(plaintext);
    
    let ciphertext = cipher.encrypt(&mut buffer, pos).unwrap();
    
    println!("\nCiphertext: {:?}",hex::encode(ciphertext));
    
    
    let cipher = Aes128Cbc::new_from_slices(&key, &iv).unwrap();
    //convert ciphertext std::struct::vec
    let ciphertext = hex::decode(hex::encode(ciphertext)).expect("Decoding failed");
    //convert cipertext to base64 string
    let ciphertext = String::from_utf8(ciphertext).unwrap();
    
    //and return it
    return ciphertext;
    
    }


    /*
//create method for http request
fn httpRequest(url: &str) -> String {
    //create a client
    let client = client::Client::new();
    //create a request
    let mut request = client.get(url);
    //send the request
    let mut response = request.send().unwrap();
    //read the response
    let mut body = String::new();
    response.read_to_string(&mut body).unwrap();
    //return the response
    return body;
}


*/