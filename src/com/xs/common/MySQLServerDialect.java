package com.xs.common;

import java.sql.Types;

import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.type.StandardBasicTypes;

public class MySQLServerDialect extends SQLServer2008Dialect {

	public MySQLServerDialect() {
		super();
		registerHibernateType(1, "string");
		registerHibernateType(-9, "string");
		registerHibernateType(-16, "string");
		registerHibernateType(3, "double");

		registerHibernateType(Types.CHAR, StandardBasicTypes.STRING.getName());
		registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());
		registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.STRING.getName());
		registerHibernateType(Types.DECIMAL, StandardBasicTypes.DOUBLE.getName());
	}

}
