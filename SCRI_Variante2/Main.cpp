// Need to link with Ws2_32.lib
#pragma comment (lib, "Ws2_32.lib")
#pragma comment(lib, "crypt32.lib")
#pragma comment (lib, "Advapi32.lib")
#pragma comment( linker, "/subsystem:console" )
// #pragma comment (lib, "Mswsock.lib")

#define NOCRYPT
#define WIN32_LEAN_AND_MEAN

#include <Windows.h>
#include <Wincrypt.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <stdlib.h>
#include <stdio.h>
#include <string>
#include <iostream>
#include <sstream>
#include <algorithm>
#include <ctype.h>
#include <deque>
#include <vector>
#include <cstring>
#include <math.h>
#include <locale>
#include<ctime>



#define DEFAULT_BUFLEN 512
#define DEFAULT_PORT "6792"
#define INVALID_DOUBLE -1
#define MIN_SENSOR_RECORD 6
#define MAX_SENSOR_RECORD 12
#define PI 3.14159265
#define LOWERBOUND_DOMAIN 0.8
#define UPPERBOUND_DOMAIN 6
#define LOWERBOUND_AT -1
#define UPPERBOUND_AT 10
using namespace std;
deque<double> sensor1_data;
deque<double> sensor2_data;
enum HashType{ HashSha1, HashMd5, HashSha256 };

#define TICKS_PER_SECOND 10000000
#define EPOCH_DIFFERENCE 11644473600LL

string GetHashText(const void * data, const size_t data_size, HashType hashType)
{
	HCRYPTPROV hProv = NULL;

	if (!CryptAcquireContext(&hProv, NULL, NULL, PROV_RSA_AES, CRYPT_VERIFYCONTEXT)) {
		return "";
	}

	BOOL hash_ok = FALSE;
	HCRYPTPROV hHash = NULL;
	switch (hashType) {
	case HashSha1: hash_ok = CryptCreateHash(hProv, CALG_SHA1, 0, 0, &hHash); break;
	case HashMd5: hash_ok = CryptCreateHash(hProv, CALG_MD5, 0, 0, &hHash); break;
	case HashSha256: hash_ok = CryptCreateHash(hProv, CALG_SHA_256, 0, 0, &hHash); break;
	}

	if (!hash_ok) {
		CryptReleaseContext(hProv, 0);
		return "";
	}

	if (!CryptHashData(hHash, static_cast<const BYTE *>(data), data_size, 0)) {
		CryptDestroyHash(hHash);
		CryptReleaseContext(hProv, 0);
		return "";
	}

	DWORD cbHashSize = 0, dwCount = sizeof(DWORD);
	if (!CryptGetHashParam(hHash, HP_HASHSIZE, (BYTE *)&cbHashSize, &dwCount, 0)) {
		CryptDestroyHash(hHash);
		CryptReleaseContext(hProv, 0);
		return "";
	}

	std::vector<BYTE> buffer(cbHashSize);
	if (!CryptGetHashParam(hHash, HP_HASHVAL, reinterpret_cast<BYTE*>(&buffer[0]), &cbHashSize, 0)) {
		CryptDestroyHash(hHash);
		CryptReleaseContext(hProv, 0);
		return "";
	}

	std::ostringstream oss;

	for (std::vector<BYTE>::const_iterator iter = buffer.begin(); iter != buffer.end(); ++iter) {
		oss.fill('0');
		oss.width(2);
		oss << std::hex << static_cast<const int>(*iter);
	}

	CryptDestroyHash(hHash);
	CryptReleaseContext(hProv, 0);
	return oss.str();
}


double getDoubleNumber(string input)
{
	try
	{
		return stod(input);
	}
	catch (const std::invalid_argument& ia) {
		return INVALID_DOUBLE;
	}
}

double gluc(double value){
	return (-3.4 + (1.354 * value) + (1.545 * tan(pow(value,0.25))));
}

double calc_doses(double g, double dg, double ddg){
	return 0;
}

time_t convertWindowsTimeToUnixTime(long long int input){
	long long int temp;
	temp = input / TICKS_PER_SECOND; //convert from 100ns intervals to seconds;
	temp = temp - EPOCH_DIFFERENCE;  //subtract number of seconds between epochs
	return (time_t)temp;
}

string processValues(int iteration_number, string timestamp, double sensor1_t1, double sensor2_t1, double sensor1_t2, double sensor2_t2, double sensor1_t3, double sensor2_t3, double insulinaAtual)
{
	int numOcc1, numOcc2;
	int numDoses;
	bool trust1, trust2;
	double var1, var2, var3, g1, g2, g3, dg;
	numOcc1 = numOcc2 = 0;
	trust1 = trust2 = true;
	
	//VERIFICA플O DO DOMINIO DO INPUT
	if (sensor1_t1 < LOWERBOUND_DOMAIN || sensor1_t1 > UPPERBOUND_DOMAIN) sensor1_t1 = -1 ;
	if (sensor1_t2 < LOWERBOUND_DOMAIN || sensor1_t2 > UPPERBOUND_DOMAIN) sensor1_t2 = -1 ;
	if (sensor1_t3 < LOWERBOUND_DOMAIN || sensor1_t3 > UPPERBOUND_DOMAIN) sensor1_t3 = -1 ;
	
	if (sensor2_t1 < LOWERBOUND_DOMAIN || sensor2_t1 > UPPERBOUND_DOMAIN) sensor2_t1 = -1 ;
	if (sensor2_t2 < LOWERBOUND_DOMAIN || sensor2_t2 > UPPERBOUND_DOMAIN) sensor2_t2 = -1 ;
	if (sensor2_t3 < LOWERBOUND_DOMAIN || sensor2_t3 > UPPERBOUND_DOMAIN) sensor2_t3 = -1 ;

	// VERIFICAR QUANTIDADE DE INFORMA플O
	if (((sensor1_t1 == -1) && (sensor2_t1 == -1)) || ((sensor1_t2 == -1) && (sensor2_t2 == -1)) || ((sensor1_t2 == -1) && (sensor2_t2 == -1)))
	{
		cout << "[ERRO]: SEM INFORMA플O SUFICIENTE " << endl << "       AMBOS OS SENSORES FALHARAM NO MESMO INSTANTE T" << endl;
		numDoses = -1;
	}
	else
	{
		
			// VERIFICAR INTEGRIDADE SENSORES - STUCK AT'S
			if (sensor1_t1 != -1) sensor1_data.push_back(sensor1_t1);
			if (sensor1_t2 != -1) sensor1_data.push_back(sensor1_t2);
			if (sensor1_t3 != -1) sensor1_data.push_back(sensor1_t3);

			if (sensor1_data.size() >= MIN_SENSOR_RECORD)
			{
				numOcc1 = std::count(sensor1_data.begin(), sensor1_data.end(), sensor1_t1);
				if (numOcc1 == sensor1_data.size()) trust1 = false;
				numOcc1 = std::count(sensor1_data.begin(), sensor1_data.end(), sensor1_t2);
				if (numOcc1 == sensor1_data.size()) trust1 = false;
				numOcc1 = std::count(sensor1_data.begin(), sensor1_data.end(), sensor1_t3);
				if (numOcc1 == sensor1_data.size()) trust1 = false;
			}
			else trust1 = true;

		
			if (sensor2_t1 != -1) sensor2_data.push_back(sensor2_t1);
			if (sensor2_t2 != -1) sensor2_data.push_back(sensor2_t2);
			if (sensor2_t3 != -1) sensor2_data.push_back(sensor2_t3);
			if (sensor2_data.size() >= MIN_SENSOR_RECORD)
			{
				numOcc2 = std::count(sensor2_data.begin(), sensor2_data.end(), sensor2_t1);
				if (numOcc2 == sensor2_data.size()) trust2 = false;
				numOcc2 = std::count(sensor2_data.begin(), sensor2_data.end(), sensor2_t2);
				if (numOcc2 == sensor2_data.size()) trust2 = false;
				numOcc2 = std::count(sensor2_data.begin(), sensor2_data.end(), sensor2_t3);
				if (numOcc2 == sensor2_data.size()) trust2 = false;
			}
			else trust2 = true;

		if (trust1 == false && trust2 == false) // AMBOS FALHAM
		{
			cout << "[ERRO]: AMBOS OS SENSORES STUCK-AT" << endl;
			numDoses = -1;
		}
		else
		{
			if ((trust1 == true) && (trust2 == false)) // USA APENAS SENSOR 1
			{
				cout << "[ERRO]: SENSOR 1 STUCK-AT .." << endl;
				var1 = sensor1_t1;
				var2 = sensor1_t2;
				var3 = sensor1_t3;

			}
			else if ((trust1 == false) && (trust2 = true)) // USA APENAS SENSOR 2
			{
				cout << "[ERRO]: SENSOR 2 STUCK-AT .." << endl;
				var1 = sensor2_t1;
				var2 = sensor2_t2;
				var3 = sensor2_t3;

			}
			else // AMBOS OS SENSOSRES OK. FAZ MEDIA
			{
				cout << "AMBOS OS SENSORES OK" << endl;
				if (sensor1_t1 == -1) var1 = sensor2_t1;
				else if (sensor2_t1 == -1) var1 = sensor1_t1;
				else var1 = (sensor1_t1 + sensor2_t1) / 2;

				if (sensor1_t2 == -1) var2 = sensor2_t2;
				else if (sensor2_t2 == -1) var2 = sensor1_t2;
				else var2 = (sensor1_t2 + sensor2_t2) / 2;
				
				if (sensor1_t3 == -1) var3 = sensor2_t3;
				else if (sensor2_t3 == -1) var3 = sensor1_t3;
				else var3 = (sensor1_t3 + sensor2_t3) / 2;

			}

			g1 = gluc(var1);
			g2 = gluc(var2);
			g3 = gluc(var3);

			dg = g3 - g2;

			if (g3 < 6.0)
			{
				// Nao e preciso injectar insulina porque os niveis de glucose estao normais
				numDoses = 0;
			}
			else if (g3 >= 6.0 && dg < -0.4)
			{
				// Nao e preciso injectar insulina porque os niveis de glucose ja estao a descer
				numDoses = 0;
			}
			else if (g3 >= 6.0 && dg >= -0.4)
			{
				// E necessario calcular o numero de doses
				double ddg = (g2 - g1) - dg; //CONFIRMAR QUE NAO EST� AO CONTRARIO
				numDoses = (int)ceil((0.8 * g3) + (0.2 * dg) + (0.5 * ddg) - insulinaAtual); //VERIFICAR CEIl
			}

		}


		// LIMPAR DEQUES
		if (sensor1_data.size() == MAX_SENSOR_RECORD)
		{
			sensor1_data.pop_front();
			sensor1_data.pop_front();
			sensor1_data.pop_front();
		}
		if (sensor2_data.size() == MAX_SENSOR_RECORD)
		{
			sensor2_data.pop_front();
			sensor2_data.pop_front();
			sensor2_data.pop_front();
		}
	}

	//TESTE ACEITACAO
	if(numDoses < LOWERBOUND_AT || numDoses > UPPERBOUND_AT) numDoses = -1;

	// Retornar mensagem a enviar
	string sentence = "putresult ";
	std::time_t s_timestamp = std::time(0) * 1000;  // t is an integer type
	sentence += to_string(iteration_number) + " ";
	sentence += to_string(s_timestamp) + " ";
	sentence += to_string(numDoses);
	const char* temp = sentence.c_str();
	string hash = GetHashText(temp, strlen(temp), HashMd5);
	sentence += " ";
	sentence += hash;
	sentence += "\n";
	//cout << "sentence function" << sentence << endl;
	return sentence;
}


int __cdecl main(void)
{
	WSADATA wsaData;
	int iResult;

	SOCKET ListenSocket = INVALID_SOCKET;
	SOCKET ClientSocket = INVALID_SOCKET;

	struct addrinfo *result = NULL;
	struct addrinfo hints;

	//char recvbuf[DEFAULT_BUFLEN];
	int recvbuflen = DEFAULT_BUFLEN;

	// Initialize Winsock
	iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != 0) {
		printf("WSAStartup failed with error: %d\n", iResult);
		return 1;
	}

	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	// Resolve the server address and port
	iResult = getaddrinfo(NULL, DEFAULT_PORT, &hints, &result);
	if (iResult != 0) {
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		return 1;
	}

	// Create a SOCKET for connecting to server
	ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
	if (ListenSocket == INVALID_SOCKET) {
		printf("socket failed with error: %ld\n", WSAGetLastError());
		freeaddrinfo(result);
		WSACleanup();
		return 1;
	}

	// Setup the TCP listening socket
	iResult = bind(ListenSocket, result->ai_addr, (int)result->ai_addrlen);
	if (iResult == SOCKET_ERROR) {
		printf("bind failed with error: %d\n", WSAGetLastError());
		freeaddrinfo(result);
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	freeaddrinfo(result);

	iResult = listen(ListenSocket, SOMAXCONN);
	if (iResult == SOCKET_ERROR) {
		printf("listen failed with error: %d\n", WSAGetLastError());
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	// Accept a client socket
	ClientSocket = accept(ListenSocket, NULL, NULL);
	if (ClientSocket == INVALID_SOCKET) {
		printf("accept failed with error: %d\n", WSAGetLastError());
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	// No longer need server socket
	closesocket(ListenSocket);
	char* sentence;
	string temp;
	// Receive until the peer shuts down the connection
	do {
		char inputchar = '1';
		int posicao = 0;
		sentence = new char[500];

		recv(ClientSocket, &inputchar, 1, 0);
		sentence[posicao++] = inputchar;

		while (inputchar != '\n')
		{
			recv(ClientSocket, &inputchar, 1, 0);
			sentence[posicao++] = inputchar;
		}
		sentence[posicao] = '\0';
		//cout << "count: " << posicao << endl; 
		cout << "[RECEIVED]: " << sentence;
		//system("pause");
		string sentence_str(sentence);
		istringstream iss(sentence_str);

		temp = "";
		iss >> temp;

		if (temp == "putdata")
		{
			int iteration_number;
			string timestamp, inputHash, alteredHash;
			double sensor1_t1, sensor1_t2, sensor1_t3, sensor2_t1, sensor2_t2, sensor2_t3, insulinaAtual;
			string input_string_altered = "putdata ";
			sensor1_t1 = sensor1_t2 = sensor1_t3 = sensor2_t1 = sensor2_t2 = sensor2_t3 = -1;
			insulinaAtual = 0;

			iss >> temp; iteration_number = stoi(temp);  input_string_altered += temp + " ";

			iss >> timestamp; input_string_altered += timestamp + " ";

			iss >> temp; sensor1_t1 = getDoubleNumber(temp); input_string_altered += temp + " ";

			iss >> temp; sensor2_t1 = getDoubleNumber(temp); input_string_altered += temp + " ";

			iss >> temp; sensor1_t2 = getDoubleNumber(temp); input_string_altered += temp + " ";

			iss >> temp; sensor2_t2 = getDoubleNumber(temp); input_string_altered += temp + " ";

			iss >> temp; sensor1_t3 = getDoubleNumber(temp); input_string_altered += temp + " ";

			iss >> temp; sensor2_t3 = getDoubleNumber(temp); input_string_altered += temp + " ";

			iss >> temp; insulinaAtual = getDoubleNumber(temp); input_string_altered += temp;

			iss >> inputHash;

			const char* tempCString = input_string_altered.c_str();

			alteredHash = GetHashText(tempCString, strlen(tempCString), HashMd5);

			if (inputHash == alteredHash)
			{
				string reply = processValues(iteration_number, timestamp, sensor1_t1, sensor2_t1, sensor1_t2, sensor2_t2, sensor1_t3, sensor2_t3, insulinaAtual);
				const char* tempCstr = reply.c_str();
				iResult = send(ClientSocket, tempCstr, strlen(tempCstr), 0);
				cout << "[SENT]: " << reply << endl;

			}
			else
			{
				cout << "Verificacao da Hash recebida falhou" << endl;
			}
		}
	} while (temp != "end");

	char* ackStr = "ack";
	send(ClientSocket, ackStr, strlen(ackStr), 0);
	cout << "[SENT]: " << ackStr << endl;

	// shutdown the connection since we're done
	shutdown(ClientSocket, SD_SEND);
	closesocket(ClientSocket);
	WSACleanup();
	cout << "Processo terminou com sucesso." << endl;
	system("pause");

	return 0;
}