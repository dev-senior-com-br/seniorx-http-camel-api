package br.com.senior.seniorx.http.camel;

public enum PrimitiveType {

    ACTION("actions"), //
    QUERY("queries"), //
    SIGNAL("signals"), //
    ENTITIES("entities"); //

    final String path;

    PrimitiveType(String path) {
        this.path = path;
    }

}
