```text
8888888888 888                   888    d8b                                                     888      
888        888                   888    Y8P                                                     888      
888        888                   888                                                            888      
8888888    888  8888b.  .d8888b  888888 888  .d8888b .d8888b   .d88b.   8888b.  888d888 .d8888b 88888b.  
888        888     "88b 88K      888    888 d88P"    88K      d8P  Y8b     "88b 888P"  d88P"    888 "88b 
888        888 .d888888 "Y8888b. 888    888 888      "Y8888b. 88888888 .d888888 888    888      888  888 
888        888 888  888      X88 Y88b.  888 Y88b.         X88 Y8b.     888  888 888    Y88b.    888  888 
8888888888 888 "Y888888  88888P'  "Y888 888  "Y8888P  88888P'  "Y8888  "Y888888 888     "Y8888P 888  888 
```
-- 

### 1. Install Elasticsearch on MacOS

- [Download Elasticsearch](https://www.elastic.co/downloads/elasticsearch)


```bash
$ wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.11.4-darwin-x86_64.tar.gz
```


- Unzip file `elasticsearch-8.11.4-darwin-x86_64.tar.gz`
```bash
  $ tar -xzf elasticsearch-8.11.4-darwin-x86_64.tar.gz
```


- Run Elasticsearch
```bash
  $ cd elasticsearch-8.11.4
  $ bin/elasticsearch
```


- Elasticsearch run successfully as below:
```
✅ Elasticsearch security features have been automatically configured!
✅ Authentication is enabled and cluster connections are encrypted.

ℹ️  Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
  -RzbvuQfhkrT74rOM=1q

ℹ️  HTTP CA certificate SHA-256 fingerprint:
  7d5bcd9133fec7a89edbb76688e1ba60cafb23f7cdd35d97090abc4f6725a554

ℹ️  Configure Kibana to use this cluster:
• Run Kibana and click the configuration link in the terminal when Kibana starts.
• Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
  eyJ2ZXIiOiI4LjExLjQiLCJhZHIiOlsiMTcyLjE2LjAuMjo5MjAwIl0sImZnciI6IjdkNWJjZDkxMzNmZWM3YTg5ZWRiYjc2Njg4ZTFiYTYwY2FmYjIzZjdjZGQzNWQ5NzA5MGFiYzRmNjcyNWE1NTQiLCJrZXkiOiJHeHo5RDQwQjU4N016cHdWaG1uSDptT3gyaVdXdVFlR3dxb1hTYi04TDRBIn0=

ℹ️  Configure other nodes to join this cluster:
• On this node:
  ⁃ Create an enrollment token with `bin/elasticsearch-create-enrollment-token -s node`.
  ⁃ Uncomment the transport.host setting at the end of config/elasticsearch.yml.
  ⁃ Restart Elasticsearch.
• On other nodes:
  ⁃ Start Elasticsearch with `bin/elasticsearch --enrollment-token <token>`, using the enrollment token that you generated.
```


- Login [Elasticsearch](https://localhost:9200/) successfully as below:
```
  {
    "name" : "MacBook-Pro.local",
    "cluster_name" : "elasticsearch",
    "cluster_uuid" : "-VD4nrH9RoqjbrBnIFKhhA",
    "version" : {
      "number" : "8.11.4",
      "build_flavor" : "default",
      "build_type" : "tar",
      "build_hash" : "da06c53fd49b7e676ccf8a32d6655c5155c16d81",
      "build_date" : "2024-01-08T10:05:08.438562403Z",
      "build_snapshot" : false,
      "lucene_version" : "9.8.0",
      "minimum_wire_compatibility_version" : "7.17.0",
      "minimum_index_compatibility_version" : "7.0.0"
    },
    "tagline" : "You Know, for Search"
  }
```
