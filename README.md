# MyORM
TP de développement pour le cours EPSI S3 ORM.

**Sujet** : Développer un ORM en Java prenant en charge les fonctionnalités simples.

L'ORM sera capable d'utiliser java existante et de prendre un ensemble de `Class` à gérer en entités.  
Le mapping sera configuré via des annotations Java.  

Pour faciliter la réalisation du TP un ensemble de librairies pour faciliter la manipulation de l'api de Reflexivité Java ainsi que les paramètres nommés sont fournis dans le package [`fr.epsi.orm.myorm.lib`](src/main/java/fr/epsi/orm/myorm/lib).  
Des exemples d'utilisation de la librarie sont fournis en [sample](src/main/java/fr/epsi/orm/myorm/lib/sample/Samples.java)

## Specifications 

Configuration par annotation : 
 - [@Entity](src/main/java/fr/epsi/orm/myorm/annotation/Entity.java)
 - [@Column](src/main/java/fr/epsi/orm/myorm/annotation/Column.java)
 - [@Transient](src/main/java/fr/epsi/orm/myorm/annotation/Transient.java)
 - [@Id](src/main/java/fr/epsi/orm/myorm/annotation/Id.java)

Interface de manipulation de la base de données : [EntityManager](src/main/java/fr/epsi/orm/myorm/persistence/EntityManager.java)  
Check des classes persistantes lors de l'instanciation d'un `EntityManager` :
 - Chaque classe persistante doit être annoté avec l'annotation `@Entity`
 - Chaque classe doit avoir un et un seul champs annoté avec `@Id`

## TP

Une implémentation de l'interface `EntityManager` a été commencé mais pas finie.  
Codez les fonctionnalités manquantes pour faire passer les tests de l'application au vert.

Classes à compléter : 
 - [`BasicEntityManager`](src/main/java/fr/epsi/orm/myorm/persistence/BasicEntityManager.java) Implementation de l'interface `EntityManager`
 - [`MappingHelper`](src/main/java/fr/epsi/orm/myorm/persistence/MappingHelper.java) Utilitaires de mapping
 - [`SqlGenerator`](src/main/java/fr/epsi/orm/myorm/persistence/SqlGenerator.java) Utilitaires de generation du SQL
 
Vous pouvez vous baser sur le code de la fonctionnalité `delete` existante pour réaliser les autres opérations...  

Les tests sont réalisés avec une base embarqués utilisant ce [script](src/test/resources/init-db.sql) d'initialisation et en utilisant la classe [`User`](src/main/java/fr/epsi/orm/myorm/lib/sample/User.java).

## Javadoc

La Javadoc du projet est disponible également dans le repo : [apidocs](apidocs)  
Vous pouvez ouvrir le fichier `apidocs/index.html` dans votre navigateur en local pour vidualiser la javadoc.
