//package me.hysong.libcodablejdbc.utils.objects;
//
//import lombok.Getter;
//import lombok.Setter;
//import me.hysong.libcodablejdbc.utils.exceptions.InitializationViolationException;
//import me.hysong.libcodablejdbc.utils.exceptions.JDBCReflectionGeneralException;
//
//import java.util.Base64;
//
//@Getter
//@Setter
//public class DbPtr<T> {
//
//    private String database;
//    private String table;
//    private String column;
//    private String pk;
//    private Object pkValue;
//
//    private T actualValue;
//
//    public DbPtr(){}
//
//    public DbPtr(DatabaseRecord recordEntity, String column) throws InitializationViolationException, JDBCReflectionGeneralException {
//        this.database = recordEntity.getDatabase();
//        this.table = recordEntity.getTable();
//        this.column = column;
//        this.pk = recordEntity.getPrimaryKeyColumnName();
//        this.pkValue = recordEntity.getPrimaryKeyValue();
//    }
//
//    public DbPtr(String encodedString) {
//        // Total 6 segments based on @ symbol
//        String[] params = encodedString.split("@");
//        if (params.length != 6) {
//            throw new IllegalArgumentException("Incorrect pointer size");
//        }
//
//        // Starting header / ending
//        if (!params[0].equals("&{dbptr}")) {
//            throw new IllegalArgumentException("Incorrect pointer header");
//        } else if (!params[5].equals("{dbptr}")) {
//            throw new IllegalArgumentException("Incorrect pointer tail");
//        }
//
//        // Decode
//        column = decodeB64(params[1]);
//        table = decodeB64(params[4]);
//        database = decodeB64(params[3]);
//
//        String[] pkComponents = params[2].split("--");
//        pk = decodeB64(pkComponents[0]);
//        pkValue = decodeB64(pkComponents[1]);
//    }
//
//    // Lazy loading
//    public void load() {
//
//    }
//
//    private void loadRecursively(int maxDepth, int currentDepth) {
//        // 나가기
//        if (currentDepth == maxDepth) {
//            return;
//        }
//
//        // TODO Load actually from Db
//        load();
//
//        // 만약 dbptr 이 아니라면
//        if (!(actualValue instanceof DbPtr<?>)) {
//            System.out.println("[DbPtr] Reached end of loadRecursively function.");
//            return;
//        }
//
//        ((DbPtr<?>) actualValue).loadRecursively(maxDepth, currentDepth + 1);
//    }
//
//    public void loadRecursively(int maxDepth) {
//        loadRecursively(maxDepth, 0);
//    }
//
//    private String encodeB64(String s) { return Base64.getEncoder().encodeToString(s.getBytes()); }
//    private String decodeB64(String s) { return new String(Base64.getDecoder().decode(s)); }
//
//    @Override
//    public String toString() {
//        return "&{dbptr}@" + encodeB64(column) + "@" + encodeB64(pk) + "--" + encodeB64(pkValue.toString()) + "@" + encodeB64(table) + "@" + encodeB64(database) + "@{dbptr}";
//    }
//
//}
