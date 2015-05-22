#include<iostream>
#include<string>
#include <sstream>
using namespace std;


int messageParser(string input)
{
	istringstream iss(input);
	do
	{
		string partial ;
		iss >> partial ;
		cout << "partial: " << partial << endl;
	}while(iss);

}


int main() {
	messageParser("1 0 20140522114203 0.1 0.1 0.1 0.1 0.1 0.1 3ec1a92c2ca8275a9a0533c042cc9583");
	return 0 ;
	
	//http://www.tutorialspoint.com/unix_sockets/socket_server_example.htm
}


