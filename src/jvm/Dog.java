package jvm;

public class Dog {
    public Dog() {
        System.out.println("Dog is loader by : " + this.getClass().getClassLoader());
    }
}
