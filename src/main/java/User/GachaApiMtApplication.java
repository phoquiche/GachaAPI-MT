package User;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class GachaApiMtApplication {

    public static void main(String[] args) {
        SpringApplication.run(GachaApiMtApplication.class, args);
    }

    public static MongoCollection<Document> getMongo(String collectionName) {
        MongoClient mongoClient = MongoClients.create("mongodb://www.soybank.fr:27017");
        MongoDatabase database = mongoClient.getDatabase("gacha");
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection;
    }






}
@RestController
@RequestMapping("/api")
class GachaController {
    @GetMapping("/gacha")
    public String gacha() {

       MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
       Document doc = collection.find().first();
        assert doc != null;
        return doc.toJson();

    }
}