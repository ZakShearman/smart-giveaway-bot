package pink.zak.giveawaybot.service.storage.backends.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import pink.zak.giveawaybot.service.storage.settings.StorageSettings;

public class MongoConnectionFactory {
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    public MongoConnectionFactory(StorageSettings storageSettings) {
        ServerAddress address = new ServerAddress(storageSettings.getHost(), Integer.parseInt(storageSettings.getPort()));
        if (storageSettings.getPassword().isEmpty()) {
            this.mongoClient = new MongoClient(address);
        } else {
            MongoCredential credential = MongoCredential.createCredential(storageSettings.getUsername(), storageSettings.getDatabase(), storageSettings.getPassword().toCharArray());
            this.mongoClient = new MongoClient(address, credential, new MongoClientOptions.Builder().build());
        }
        this.mongoDatabase = this.mongoClient.getDatabase(storageSettings.getDatabase());
    }

    public MongoDatabase getDatabase() {
        return this.mongoDatabase;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        for (String loopedCollection : this.mongoDatabase.listCollectionNames()) {
            if (loopedCollection.equals(collectionName)) {
                return this.mongoDatabase.getCollection(collectionName);
            }
        }
        this.mongoDatabase.createCollection(collectionName);
        return this.mongoDatabase.getCollection(collectionName);
    }

    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }
}
