package fr.epsi.orm.myorm.persistence;

import fr.epsi.orm.myorm.annotation.Entity;
import fr.epsi.orm.myorm.annotation.Id;
import fr.epsi.orm.myorm.annotation.Transient;
import fr.epsi.orm.myorm.lib.NamedPreparedStatement;
import fr.epsi.orm.myorm.lib.ReflectionUtil;
import javaslang.Predicates;

import javax.sql.DataSource;
import javax.swing.text.html.Option;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static fr.epsi.orm.myorm.persistence.MappingHelper.*;
import static fr.epsi.orm.myorm.persistence.SqlGenerator.*;

/**
 * Created by fteychene on 14/05/17.
 */
public class BasicEntityManager implements EntityManager {

    private final DataSource datasource;
    private final Set<Class<?>> persistentClasses;


    private BasicEntityManager(DataSource aDataSource, Set<Class<?>> aPersistentClasses) {
        datasource = aDataSource;
        persistentClasses = aPersistentClasses;
    }

    /**
     * Check the Persistent classes to be managed by the EntityManager to have the minimal configuration.
     *
     * Each class should respect the following rules :
     *  - Class should be annotated with @Entity
     *  - Class should have one and only one field with the @Id annotation
     *
     * @param persistentClasses
     * @throws IllegalArgumentException if a class does not match the conditions
     */
    private static void checkPersistentClasses(Set<Class<?>> persistentClasses) {
        persistentClasses.forEach(entityClass -> {
            ReflectionUtil.getAnnotationForClass(entityClass, Entity.class)
                .orElseThrow(() -> new IllegalArgumentException("Illegal class passed to EntityManager"));
            if (ReflectionUtil.getFieldsDeclaringAnnotation(entityClass, Id.class)
                .count() != 1) {
                throw new IllegalArgumentException("c'est pas bon");
            }
        });
        //for (Class<?> entityClass: persistentClasses) {
        //}
    }

    /**
     * Check id a Class is managed by this EntityManager
     * @param checkClass
     */
    private void isManagedClass(Class<?> checkClass) throws IllegalArgumentException {
        if (!persistentClasses.contains(checkClass)) {
            throw new IllegalArgumentException("The class "+checkClass.getName()+" is not managed by this EntityManager ...");
        }
    }

    /**
     * Create a BasicEntityManager and check the persistents classes
     * @param dataSource The Datasource to use for connecting to DB
     * @param persistentClasses The Set of Classes to be managed in this EntityManager
     * @return The BasicEntityManager created
     */
    public static BasicEntityManager create(DataSource dataSource, Set<Class<?>> persistentClasses) {
        checkPersistentClasses(persistentClasses);
        return new BasicEntityManager(dataSource, persistentClasses);
    }

    /**
     * @see EntityManager#find(Class, Object)
     */
    @Override
    public <T> Optional<T> find(Class<T> entityClass, Object id) throws SQLException {
        isManagedClass(entityClass);

        NamedPreparedStatement statement = NamedPreparedStatement.prepare(datasource.getConnection(), "SELECT * FROM " +
                SqlGenerator.getTableForEntity(entityClass) + " WHERE id = '" + id.toString() + "'");

        ResultSet res = statement.executeQuery();
        res.next();
        return Optional.of(MappingHelper.mapToInstance(res, entityClass));
    }

    /**
     * @see EntityManager#findAll(Class)
     */
    @Override
    public <T> List<T> findAll(Class<T> entityClass) throws SQLException, IllegalArgumentException {
        isManagedClass(entityClass);
        NamedPreparedStatement statement = NamedPreparedStatement.prepare(datasource.getConnection(), "SELECT * FROM " +
                SqlGenerator.getTableForEntity(entityClass));

        return MappingHelper.mapFromResultSet(entityClass, statement.executeQuery());
    }

    /**
     * @see EntityManager#save(Object)
     */
    @Override
    public <T> Optional<T> save(T entity) throws SQLException {
        isManagedClass(entity.getClass());

        ArrayList<String> fieldNames = new ArrayList<>();
        ArrayList<String> fieldValues = new ArrayList<>();

        Map<String, Object> map = MappingHelper.entityToParams(entity);

        map.keySet().forEach(key -> {
            fieldNames.add(key);
        });

        map.values().forEach(value -> {
            fieldValues.add(value.toString());
        });

        // force la génération auto d'ID
        boolean defaultId = true;

        String i = "INSERT INTO "
                + SqlGenerator.getTableForEntity(entity.getClass())
                + " ("
                + (defaultId ? "id, " : "")
                + String.join(", ", fieldNames)
                + ") VALUES ("
                + (defaultId ? "default, " : "")
                + "'"
                + String.join("', '", fieldValues)
                + "')";
        System.out.println(i);
        NamedPreparedStatement statement = NamedPreparedStatement.prepareWithKeys(datasource.getConnection(), i);

        statement.execute();
        ResultSet rs = statement.getGeneratedKeys();
        rs.next();
        Long generatedId = rs.getLong(1);
        ReflectionUtil.setValue(ReflectionUtil.getFieldByName(entity.getClass(), "id").get(), entity, generatedId);

        return Optional.of(entity);
    }

    /**
     * @see EntityManager#delete(Object)
     */
    @Override
    public <T> boolean delete(T entity) {
        isManagedClass(entity.getClass());
        try {
            Field idField = ReflectionUtil.getFieldDeclaringAnnotation(entity.getClass(), Id.class).get();
            int affectedRows = executeUpdate(SqlGenerator.generateDeleteSql(entity.getClass()), new HashMap<String, Object>() {{
                put(idField.getName(), ReflectionUtil.getValue(idField, entity).get());
            }});
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private <T> int executeUpdate(String sql, Map<String, Object> parameters) throws SQLException {
        NamedPreparedStatement statement = NamedPreparedStatement.prepare(datasource.getConnection(), sql);
        statement.setParameters(parameters);
        return statement.executeUpdate();
    }
}
