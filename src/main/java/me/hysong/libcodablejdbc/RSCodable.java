package me.hysong.libcodablejdbc;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public interface RSCodable {

    default RSCodable objectifyCurrentRow(ResultSet rs) {
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {

            // Skip unannotated field
            if (!RSCodableUtil.isMarkedMappingElement(field, RSMapping.class)) {
                continue;
            }

        }

        return this;
    }
}
