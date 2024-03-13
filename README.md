# Programming assignment 4
The application is developed with java 17 and maven, using ZMQ socket programming to establish connection between clients and server.

## download the datasets, unzip and give read write and execute access to the datasets


```bash
chmod 777 /path/to/dataset 
```

## Installation and setup

```bash
sudo apt install openjdk-17-jdk maven
git clone https://github.com/neurobazaar/csc435-pa4-arunpalanisamy98.git
```

## compilation
```bash
cd /path/to/csc435-winter2024-pa3/app-java/
mvn clean compile
mvn package

```
## running the server
```bash
mvn exec:java -Dexec.mainClass="csc435.app.FileRetrievalServer" -Dexec.args="$hostname $port_number"
```
## list
```bash
>list
```
## quit
```bash
>quit
```


## running the client
```bash
mvn exec:java -Dexec.mainClass="csc435.app.FileRetrievalClient" -Dexec.args="$hostname $port_number"
```

## indexing
```bash
>index /path/to/dataset_folder
```
## searching
```bash
>search $arg1
>search $arg1 AND $arg2
```
## quit
```bash
>quit
```


## Credtis

created by apalanis@depaul.edu(Arun Kumar palanisamy) for Programming assignment4. Instructor name: Alexandru Orhean.

