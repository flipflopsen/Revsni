pub enum CommunicationMode {
    Tcp,
    Udp, //going for password only, aes cbc or sth, salt saved via i/o in txt file
    Http,
}


pub enum EncryptionMode {
    None,
    Aes(String),
    Rsa(String),
}

pub struct ServerMode {
    pub(crate) protocol: CommunicationMode,
    pub(crate) encryption: EncryptionMode,
    pub(crate) lhost: String,
    pub(crate) lport: i16,
    pub(crate) srvhost: String,
    pub(crate) srvport: i16,
}

pub const UPDATER_PATH: &str = "./file_hosting/initial.txt";
pub const AES_PASSWORD: &str = "lol123";
pub const LHOST: &str = "127.0.0.1";
pub const LPORT: i16 = 1331;
pub const SRVHOST: &str = "127.0.0.1";
pub const SRVPORT: i16 = 8082;
pub const ENCRYPTION: EncryptionMode = EncryptionMode::None;
pub const PROTOCOL: CommunicationMode = CommunicationMode::Http;