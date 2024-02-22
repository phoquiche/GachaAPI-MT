package User;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("gacha");
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection;
    }


    public static boolean checkLevelUp(int idJoueur){
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
         if (xp>=(int)(Math.pow(1.1, level)*50)){
            levelUp(idJoueur);
            return true;
        }
         else return false;

    }

    public static void levelUp(int idJoueur){
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        //convert idJoueur to string
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
        int newLevel = level+1;
        int newXP = xp-(int)(Math.pow(1.1, level)*50);
        Document update = new Document("lvl", newLevel).append("exp", newXP).append("sizeMonster",doc.getInteger("sizeMonster")+1);
        collection = GachaApiMtApplication.getMongo("User");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));



    }
    public static boolean addXP(int idJoueur, int xp){
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int newXP = doc.getInteger("exp")+xp;
        Document update = new Document("exp", newXP);
        collection = GachaApiMtApplication.getMongo("User");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));
        return checkLevelUp(idJoueur);
    }

    public static String afficheInfoJSON(int idJoueur){
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        return doc.toJson();
    }






}
@RestController
@RequestMapping("/api")
class GachaController {
    @GetMapping("/gacha")
    public String gacha() {
       GachaApiMtApplication.addXP(001,100);
       return GachaApiMtApplication.afficheInfoJSON(001);





    }
    @GetMapping("/index.html")
    public String index(){
        return "<li><ul><a href=\"http://localhost:8080/api/gacha\">Level UP</ul></li>";
    }


}