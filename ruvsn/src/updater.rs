extern crate base64;

use crate::configuration::EncryptionMode;
use crate::configuration::CommunicationMode;
use crate::configuration::UPDATER_PATH;

use std::env;
use std::fs;
use base64::{encode, decode};

pub fn write_initial(initial : &String) {
    let out = initial.as_bytes();
    fs::write(UPDATER_PATH, encode(out)).expect("Failed to write initial updater.txt file!");
    println!("Wrote initial updater String to {}!", UPDATER_PATH);
}

