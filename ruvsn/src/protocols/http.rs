use tokio::sync::mpsc;

#[tokio::main]
pub async fn main() {
    let (tx, mut rx) = mpsc::channel(4096);
    let tx2 = tx.clone();
    //let txN = tx.clone();

    tokio::spawn(async move {

        tx.send("").await.expect("Failed sending something through a channel in request handling.");
    });

    while let Some(message) = rx.recv().await {
        println!("")
    }
}


#[derive(Debug, Deserialize, Serialize)]
pub struct Connection {
    pub id: u16,
    pub uuid: String,
    pub ip: String,
    pub os: String,
}

impl Connection {
    pub fn get_connection(&self) -> &Connection {
        return &self;
    }
    pub fn get_uuid(&self) -> &str {
        return &self.uuid;
    }
    pub fn get_ip(&self) -> &str {
        return &self.ip;
    }
    pub fn get_os(&self) -> &str {
        return &self.os;
    }
    pub fn get_alive(&self) -> &bool {
        return &self.alive;
    }
}