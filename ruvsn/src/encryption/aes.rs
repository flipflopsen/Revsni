extern crate crypto;

use rustc_serialize::hex::ToHex;
use rustc_serialize::base64::{STANDARD, ToBase64};


pub fn initialize() {
    use crate::configuration::AES_PASSWORD;
    let _password = AES_PASSWORD;

}
