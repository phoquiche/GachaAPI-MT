package User;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@SpringBootApplication
public class UserAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAPIApplication.class, args);
    }

    public static MongoCollection<Document> getMongo(String collectionName) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("gacha");
        return database.getCollection(collectionName);
    }


    public static boolean checkLevelUp(int idJoueur) {
        MongoCollection<Document> collection = UserAPIApplication.getMongo("User");
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
        MongoCollection<Document> collection = UserAPIApplication.getMongo("User");
        //convert idJoueur to string
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int level = doc.getInteger("lvl");
        int xp = doc.getInteger("exp");
        int newLevel = level + 1;
        int newXP = xp - (int) (Math.pow(1.1, level) * 50);
        Document update = new Document("lvl", newLevel).append("exp", newXP).append("sizeMonster", doc.getInteger("sizeMonster") + 1);
        collection = UserAPIApplication.getMongo("User");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));


    }

    public static void addXP(int idJoueur, int xp) {
        MongoCollection<Document> collection = UserAPIApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        int newXP = doc.getInteger("exp") + xp;
        Document update = new Document("exp", newXP);
        collection = UserAPIApplication.getMongo("User");
        collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", update));
    }

    public static Document getInfo(int idJoueur) {
        MongoCollection<Document> collection = UserAPIApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        return doc;
    }

    public static boolean addMonster(int idJoueur, int idMonstre) {
        //si on veut ajouter un monstre mais que la taille de l'équipe est déjà au max on ne peut pas
        MongoCollection<Document> collection = UserAPIApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        if (doc.getInteger("sizeMonster") >= doc.getInteger("lvl") + 10) {
            return false;
        } else {
            collection = UserAPIApplication.getMongo("User");
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$push", new Document("Monstre", new Document("id", idMonstre))));
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", new Document("sizeMonster", doc.getInteger("sizeMonster") + 1)));
            return true;


        }
    }

    public static boolean removeMonster(int idJoueur, int idMonstre) {
        MongoCollection<Document> collection = UserAPIApplication.getMongo("User");
        Document doc = collection.find(Filters.eq("id", idJoueur)).first();
        assert doc != null;
        if (doc.getInteger("sizeMonster") <= 0) {
            return false;
        } else {
            collection = UserAPIApplication.getMongo("User");
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$pull", new Document("Monstre", new Document("id", idMonstre))));
            collection.updateOne(Filters.eq("id", idJoueur), new Document("$set", new Document("sizeMonster", doc.getInteger("sizeMonster") - 1)));
            return true;
        }
    }
}
@RestController
@RequestMapping("/api")
class UserController {
    @GetMapping("/info/{id}")
    public String gacha(@PathVariable int id) {
       return UserAPIApplication.getInfo(id).toJson();
    }
    @GetMapping("/getMonsters/{id}")
    public String getMonsters(@PathVariable int id) {
        Document doc = UserAPIApplication.getInfo(id);
        List<Document> monsters = doc.getList("Monstre", Document.class);
        StringBuilder result = new StringBuilder();
        for (Document monster : monsters) {
            result.append(monster.toJson()).append("\n");
        }
        return result.toString();

    }

    @GetMapping("/addMonster/{id}/{idMonstre}")
    //addmonstre, get type and name by get request
    public String addMonster(@PathVariable int id,@PathVariable String idMonstre) {
        if (Integer.parseInt(idMonstre) == 0) {
            return "Impossible d'ajouter un monstre car l'id n'est pas valide";
        }
        if (UserAPIApplication.addMonster(id,Integer.parseInt(idMonstre))) {
            return "Monstre ajouté";
        } else {
            return "Impossible d'ajouter un monstre";
        }
    }

    @GetMapping("/removeMonster/{id}/{idMonstre}")
    public String removeMonster(@PathVariable int id, @PathVariable String idMonstre) {
        Document doc = UserAPIApplication.getInfo(id);

        if (Integer.parseInt(idMonstre) == 0) {
            return "Impossible de retirer un monstre car l'id n'est pas valide";
        } else {

            if (UserAPIApplication.removeMonster(id, Integer.parseInt(idMonstre))) {
                return "Monstre retiré, nombre de monstres actuels" + doc.getInteger("sizeMonster");
            } else {
                return "Impossible de retirer un monstre car vous n'en avez pas";
            }
        }
    }

    @GetMapping("/addXP/{id}/{xp}")
    public String addXP(@PathVariable int id, @PathVariable int xp) {
        UserAPIApplication.addXP(id, xp);
        Document doc = UserAPIApplication.getInfo(id);
        if(UserAPIApplication.checkLevelUp(id)){
            return "XP ajouté, level up, nouveau level : " + doc.getInteger("lvl");
            }
        else {
            return "XP ajouté, pas de level up, level actuel : " + doc.getInteger("lvl");
        }
    }




}
