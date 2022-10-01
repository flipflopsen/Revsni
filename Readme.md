# Revsni

**Disclaimer:** This project is for purely educational purposes! This repository stays private for several safety related reasons.

---
## Introduction

Revsni aka. **REVerse Shell Noteable Impact** aims to be a **Java-Based-Framework** type of application for different sorts of Command & Control (C2).
Furthermore it should be able to switch between different communication channels and encryption algorithms whereas encryption will be nestable (cascading) and interchangeable with open connection.

One of the main features is definetily the "switch" command, with which you can switch the communication channel and later the encryption as well.

Last but not least it will implement a builder in order to create different types of payloads and handlers, compatible with the same central server.

---
## Already implemented
### General
- Interchangeable shells
- Interchangeable and nestable encryption
### Shell types (Interchangeable)
  - TCP
  - HTTP
  - Asynchronous HTTP
  - HTTPS
  - Asynchronous HTTPS
### Encryption
  - SSL
  - AES
  - RSA
  - Blowfish
---
## Planned

### General
- Builder
  - Payloads
  - Handlers
  - Websites (for authentic HTTP(S) asynchronous stuff)
- Server
  - Local GUI
  - Web GUI

### Shell types
  - UDP
  - DNS
  - SMB
  - SSDP/UPNP

### Encryption
  - Twofish
  - Serpent
  - Triple DES
## Vocabulary and Overview
- **Revsni**      -     Java C2 Server
- **Ouchie**      -     Central Java Client
- **Cevsn**       -     CSharp Client
- **Devsn**       -     Unmanaged C++ Class Library for Injection Stuff
- **Rawvsn**      -     Ummanaged C DLL for Injection Stuff
- **Ruvsn**       -     Rust Client
- **Websn**       -     File- and Webserver for "staging"
- **Geffsn**      -     Java Client GUI for remote control C2 Server with nice RAT-like view
- **Plusn**       -     C++ Client for Revsn
- **Suvsn**       -     Windows Shellcode based Agent using Blowfish
- **Pyvsn**       -     Python Server for Windows DLL and Shellcode Agents using Blowfish

