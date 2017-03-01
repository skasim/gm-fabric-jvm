# Overview
Though String resources may be encrypted within the __gmf__ it is explicitly coded to only decrypt passwords for keystore/truststores. The default decryptor is the NoDecryptor.

# Provided decryptors

| Name | Description |
| ---- | ---- |
| NoDecryptor | Default behavior. String in == String out. |


## Configuring a different Decryptor

To enable a different decryptor, the fully qualified class name must be specified as follows:

    -com.deciphernow.server.config.resources.decryptClass=a.b.c.MyNewDecryptor
        
    
## I want to implement my own decryptor


To implement your own decryptor create a Scala class with the following __Trait:__ `com.deciphernow.server.support.Decryptor` and implement the method `def decryptResource(string: String) : String`. Then just provide the configuration parameter pointing to your decryptor and add it to `parameters.conf`.

To retrieve your decryptor do the following:


    import com.deciphernow.server.support.{DecryptorManager, Decryptor}
    
    ...
    
    lazy val decryptor : Decryptor = DecryptorManager.getInstance
    
    ...
    
    val decryptedValue : String = decryptor.decryptResource(EncryptedStringPassedHere)
    
    ...

