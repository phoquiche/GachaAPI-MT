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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    public static boolean checkLevelUp(int idJoueur) {
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
        if (xp >= (int) (Math.pow(1.1, level) * 50)) {
            levelUp(idJoueur);
            return true;
        } else return false;

    }

    public static void levelUp(int idJoueur) {
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        //convert idJoueur to string
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
        int newLevel = level + 1;
        int newXP = xp - (int) (Math.pow(1.1, level) * 50);
        Document update = new Document("lvl", newLevel).append("exp", newXP).append("sizeMonster", doc.getInteger("sizeMonster") + 1);
        collection = GachaApiMtApplication.getMongo("User");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));


    }

    public static boolean addXP(int idJoueur, int xp) {
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int newXP = doc.getInteger("exp") + xp;
        Document update = new Document("exp", newXP);
        collection = GachaApiMtApplication.getMongo("User");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));
        return checkLevelUp(idJoueur);
    }

    public static Document getInfo(int idJoueur) {
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        return doc;
    }

    public static boolean addMonster(int idJoueur, String nomMonstre,String typeMonstre) {
        //si on veut ajouter un monstre mais que la taille de l'équipe est déjà au max on ne peut pas
        MongoCollection<Document> collection = GachaApiMtApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        if (doc.getInteger("sizeMonster") >= doc.getInteger("lvl") + 10) {
            return false;
        } else {
            collection = GachaApiMtApplication.getMongo("User");
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$push", new Document("Monstre", new Document("Nom", nomMonstre) .append("Type", typeMonstre))));
            return true;


        }
    }
}
@RestController
@RequestMapping("/api")
class GachaController {
    @GetMapping("/info/{id}")
    public String gacha(@PathVariable int id) {
       return GachaApiMtApplication.getInfo(id).toJson();
    }
    @GetMapping("/getMonsters")
    public String getMonsters() {
        Document doc = GachaApiMtApplication.getInfo(001);
        List<Document> monsters = doc.getList("Monstre", Document.class);
        String result = "";
        for (Document monster : monsters) {
            result += monster.toJson() + "\n";
        }
        return result;

    }

    @GetMapping("/addMonster/{Name}/{Type}")
    //addmonstre, get type and name by get request
    public String addMonster(@PathVariable String Name, @PathVariable String Type) {
        if (GachaApiMtApplication.addMonster(001, Name, Type)) {
            return "Monstre ajouté";
        } else {
            return "Impossible d'ajouter un monstre";
        }
    }

    @GetMapping("/addXP/{id}/{xp}")
    public String addXP(@PathVariable int id, @PathVariable int xp) {
        GachaApiMtApplication.addXP(id, xp);
        Document doc = GachaApiMtApplication.getInfo(id);
        if(GachaApiMtApplication.checkLevelUp(id)){
            return "XP ajouté, level up, nouveau level : " + doc.getInteger("lvl");
            }
        else {
            return "XP ajouté, pas de level up, level actuel : " + doc.getInteger("lvl");
        }
    }


}
