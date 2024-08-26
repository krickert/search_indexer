# Getting started
Right now we're working on a docker compose file to do a single run.  This would be independent of the project since the containers are located on docker.io

Once this is run, there is a script that will trigger the end-to-end which should result in a solr collection with semantic search and two client endpoints:

1. REST endpoint for text search 
2. gRPC endpoint for text search

What this proeject does:
1) Downloads the wikipedia dumps
2) Parses them and cleans up the test
3) add sentence embeddings to the articles
4) put them in solr search


The data format between services is google protocol buffers.  These requests can be done by either an async kafka topic or through a grpc request.

The point of this project is to have a fully open source solution to download wikipedia and tweak pipeline steps to see how it affects the results of the search engine.


# Developer Notes
## useful commands
### deploy docker containers
`./mvnw -Dpackaging=docker deploy`
### Start micronaut test housing
If you're in intellij, and the unit tests are not starting right - run this
`./mvnw mn:start-testresources-service`

I'm only putting this in here because whenever I try to run micronaut tests in my jetbrains IDE, it doesn't seem to start right.  Running this makes my tests runnable in IntelliJ
=
## Dependent services
The following open source projects are needed for this to work end-to-end:
* Solr 9.6
* Kafka with schema registry
* Consul

The internal functional tests automatically use the above services to properly run.

## Wikipedia downloading
Wikimedia will only allow up to 3 concurrent connections when downloading the dump files.

For now, if a download fails, it will send the request to a failure topic.


## Downloading local models via python on ubuntu
The vectorizer by default goes out to the internet to download models.  To make tests a lot faster, we have embedded a small model to the project.

To download a file locally, you need the djl project:
git clone https://github.com/deepjavalibrary/djl.git
Online instructions in case this gets out-of-date
https://github.com/deepjavalibrary/djl/tree/master/extensions/tokenizers

```shell
# install release version of djl-converter
pip install https://publish.djl.ai/djl_converter/djl_converter-0.30.0-py3-none-any.whl

# install from djl master branch
pip install "git+https://github.com/deepjavalibrary/djl.git#subdirectory=extensions/tokenizers/src/main/python"

# install djl-convert from local djl repo
git clone https://github.com/deepjavalibrary/djl.git
cd djl/extensions/tokenizers/src/main/python
python3 -m pip install -e .

# install optimum if you want to convert to OnnxRuntime
pip install optimum

# convert a single model to TorchScript, Onnxruntime or Rust
djl-convert --help

# import models as DJL Model Zoo
djl-import --help
```


# Configuring solr to work with okta

I created a developer account and went through the motions of setting up JWT authentication on solr 9

So I'm documenting this now.

So here's how to do this from scratch:

1) Download solr
2) Start solr in cloud mode

```shell
curl -L -k https://www.apache.org/dyn/closer.lua/solr/solr/9.6.1/solr-9.6.1-src.tgz?action=download
tar zxvf solr-9.6.1.tgz
cd solr-9.6.1.tgz
```

3) To turn on the JWT configuration you need to edit the solr.in.sh
   SOLR_MODULES=jwt-auth
   SOLR_OPTS="$SOLR_OPTS -Dsolr.auth.jwt.allowOutboundHttp=true"

Add these lines at the bottom.

Note: for now we are just setting up okta authentication - if you already have SSL setup, do NOT do the -Dsolr.auth.jwt.allowOutboundHttp=true... it's just for developing...

4) Configure your okta application:
   1. Login to Okta Dashboard:
      1. Go to **https://{your-okta-app}.okta.com/** and log in with your admin credentials.
   2. Navigate to Authorization Server:
      1. Go to Security > API.
      2. Click on Authorization Servers.
      3. Select the default authorization server or the one you are using (e.g., default).
   3. Add Custom Scope:
      1. Go to the Scopes tab.
      2. Click on Add Scope.
      3. Enter the details for the custom scope:
         ```  
         Name: solr
         Display Name: Solr Access
         Description: Access to Solr
         ```
      4. Click **Save**
4) Create `security.xml` and upload it to solr.  here's my sample that worked:
   ```json

    {
      "authentication": {
        "class": "solr.JWTAuthPlugin",
        "wellKnownUrl": "https://{myoktaserver}/oauth2/default/.well-known/openid-configuration",
        "clientId": "{myclientid}",
        "redirectUris": "http://{mysolr_host}:8983/solr/",
        "blockUnknown": "true",
        "scope": ["openid", "solr"]
      },
      "authorization": {
        "class": "solr.RuleBasedAuthorizationPlugin",
        "user-role": {
          "admin": ["admin"]
        },
        "permissions": [
          {
            "name": "security-edit",
            "role": "admin"
          },
          {
            "name": "read",
            "role": "admin"
          },
          {
            "name": "write",
            "role": "admin"
          }
        ]
      }
    }

   ```

6. Upload the security.xml file to solr
   ```shell
   solr zk cp file:security.json zk:/security.json -z localhost:9983
   ```
7. restart solr

... YMMV ... this took me while to figure out.

Now the solr9 documentation claims that solrj is not compatible with this.  However, I'm working on code to change that.

# Setting up SSL

I always have to look up how to do this so I figured I'd just share a nifty script

```shell
#!/bin/bash

# Variables
KEY_FILE="./certs/solr_csr/server.key"
CERT_FILE="./certs/pem/rokkon_com.txt"
CA_BUNDLE_FILE="./certs/pem/CA Bundle.txt"
P12_OUTPUT_FILE="solr-cert.p12"
P12_PASSWORD="changeit"

# Check if the necessary files exist
if [ ! -f "$KEY_FILE" ]; then
    echo "Private key file not found: $KEY_FILE"
    exit 1
fi

if [ ! -f "$CERT_FILE" ]; then
    echo "Certificate file not found: $CERT_FILE"
    exit 1
fi

if [ ! -f "$CA_BUNDLE_FILE" ]; then
    echo "CA Bundle file not found: $CA_BUNDLE_FILE"
    exit 1
fi

# Combine the certificate and the CA bundle into a single file
COMBINED_CERT="combined-cert.pem"
cat "$CERT_FILE" "$CA_BUNDLE_FILE" > "$COMBINED_CERT"

# Generate the PKCS12 file using OpenSSL
openssl pkcs12 -export -in "$COMBINED_CERT" -inkey "$KEY_FILE" -out "$P12_OUTPUT_FILE" -name "solr" -password pass:"$P12_PASSWORD"

# Verify if the PKCS12 file has been created
if [ -f "$P12_OUTPUT_FILE" ]; then
    echo "PKCS12 file created successfully: $P12_OUTPUT_FILE"
else
    echo "Failed to create PKCS12 file"
fi

# Clean up the combined certificate file
rm "$COMBINED_CERT"

keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore solr.keystore.jks -srckeystore solr-cert.p12 -srcstoretype PKCS12

```

` ./server/scripts/cloud-scripts/zkcli.sh -zkhost localhost:9983 -cmd clusterprop -name urlScheme -val https`

This is the command to turn on https

Next we have to configure everything right....

