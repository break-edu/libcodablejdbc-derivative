package me.hysong.libcodablejdbc.dev_example;

import lombok.Getter;
import lombok.Setter;
import me.hysong.libcodablejdbc.Database;
import me.hysong.libcodablejdbc.PrimaryKey;
import me.hysong.libcodablejdbc.RSCodable;
import me.hysong.libcodablejdbc.Column;
import me.hysong.libcodablejdbc.Record;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

@Getter
@Record // This applies @Column at all fields with its own name. Name mapping will prioritize @Column(mapTo=xxx).
@Database(db="unitable", table="users")
@PrimaryKey(column="id")
public class User extends DatabaseElement implements RSCodable {

    @Column(mapTo = "unique_id") private final int id = -1;
    @Setter private String name = "John Appleseed";
    @Setter private String email = "john@apple.com";
    @Setter private int age = 23;

    public User() {
        super(new LocalSQLTableServiceSample()); // .update() .select() etc... functions will use this controller
    }

    public static void main(String[] args) throws Exception{
        User o = new User();
        o.setPrimaryKeyValue(1);
        o.select();
        System.out.println(o.getName()); // Name is loaded from database
        o.setName("Jonathan Appleseed");
        o.update(); // Update committed to database
    }
}


