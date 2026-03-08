package org.financeapp.domain;

public class Category {

    private int id;
    private String name;
    private String kind;


    public Category() {
    }

    public Category(int id, String name, String kind) {
        this.id = id;
        this.name = name;
        this.kind = kind;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", kind='" + kind + '\'' +
                '}';
    }
}
