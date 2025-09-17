import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.hysong.libcodablejdbc.Database;
import me.hysong.libcodablejdbc.PrimaryKey;
import me.hysong.libcodablejdbc.RSCodable;
import me.hysong.libcodablejdbc.RSMapping;
import me.hysong.libcodablejdbc.utils.drivers.MySQLTableService;
import me.hysong.libcodablejdbc.utils.objects.DatabaseElement;

@Getter
@RSMapping
@Database(db="unitable", table="users")
@PrimaryKey(column="id")
public class ExampleUser extends DatabaseElement implements RSCodable {

    private final int id = -1;
    @Setter private String name = "John Appleseed";
    @Setter private String email = "john@apple.com";
    @Setter private int age = 23;

    public ExampleUser() {
        super(new MySQLTableService());
    }

    public static void main(String[] args) throws Exception{
        ExampleUser o = new ExampleUser();
        o.setPrimaryKeyValue(1);
        o.select();
        System.out.println(o.getName()); // Name is loaded from database
        o.setName("Jonathan Appleseed");
        o.update(); // Update committed to database
    }
}


