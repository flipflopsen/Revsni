#include <iostream>
#include <string>

using namespace std;

int main(int argc, char *argv[])
{
	std::cout << "Hello world!" << std::endl;
}

class AES
{
	public:
		AES() { cout << "AES constructor for encryption" << endl; }
		string Encrypt() 
		{
			return NULL;
		}
		string Decrypt() 
		{
			return NULL;
		}
		void initCiphers(string pass, string salt, string iv) 
		{
			
		}
};

class Huvsn
{
	public:
		Huvsn() { cout << "Huvsn constructor for HTTP stuff" << endl;  }
		string getFile() 
		{ 
			return NULL;
		}
		string parseHostInformation() 
		{
			return NULL;
		}
};

class Tuvsn : public Huvsn, public AES
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

