package org.tensorflow.lite.examples.classification;

public class Fruit {

   private String fruit1, fruit2, fruit3;
   private String imgName;
   private String fruitID;
   private int percentage1, percentage2, percentage3;

    public Fruit() {
    }

    public Fruit(String fruit1, int percentage1) {
        this.fruit1 = fruit1;
        this.percentage1 = percentage1;
    }

    public Fruit(String fruit1, String fruit2, String fruit3, int percentage1, int percentage2, int percentage3) {
        this.fruit1 = fruit1;
        this.fruit2 = fruit2;
        this.fruit3 = fruit3;
        this.percentage1 = percentage1;
        this.percentage2 = percentage2;
        this.percentage3 = percentage3;
    }

    public String getFruit1() {
        return fruit1;
    }

    public void setFruit1(String fruit1) {
        this.fruit1 = fruit1;
    }

    public String getFruit2() {
        return fruit2;
    }

    public void setFruit2(String fruit2) {
        this.fruit2 = fruit2;
    }

    public String getFruit3() {
        return fruit3;
    }

    public void setFruit3(String fruit3) {
        this.fruit3 = fruit3;
    }

    public int getPercentage1() {
        return percentage1;
    }

    public void setPercentage1(int percentage1) {
        this.percentage1 = percentage1;
    }

    public int getPercentage2() {
        return percentage2;
    }

    public void setPercentage2(int percentage2) {
        this.percentage2 = percentage2;
    }

    public int getPercentage3() {
        return percentage3;
    }

    public void setPercentage3(int percentage3) {
        this.percentage3 = percentage3;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getFruitID() {
        return fruitID;
    }

    public void setFruitID(String fruitID) {
        this.fruitID = fruitID;
    }
}
