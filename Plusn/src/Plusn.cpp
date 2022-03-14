#include <iostream>
#include <string>

using namespace std;

int main(int argc, char *argv[])
{
	std::cout << "Hello world!" << std::endl;
}

class Tuvsn : public AES, public Huvsn
{
	string pass;
	string iv;
	string salt;
	
	public:
		Tuvsn() { cout << "Tuvsn constructor for TCP connection" << endl;}

	private:
		void connect()
		{
			cout << "Do connection stuff" << endl;
			
		}

};

class AES
{
	public:
		AES() {}
		string Encrypt() {}
		string Decrypt() {}
		void initCiphers(string pass, byte salt[], byte iv[]) {}
};

class Huvsn
{
	public:
		Huvsn() {}
		string getFile() {}
		string parseHostInformation() {}
};