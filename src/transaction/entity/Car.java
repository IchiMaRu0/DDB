package transaction.entity;

import transaction.InvalidIndexException;

import java.io.Serializable;

public class Car implements ResourceItem, Serializable {
    private String location;
    private int price;
    private int numCars;
    private int numAvail;
    private boolean isDeleted;

    public Car(String location, int price, int numCars) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
        this.numAvail = numCars;
        this.isDeleted = false;
    }

    public String getLocation() {
        return location;
    }

    public int getPrice() {
        return price;
    }

    public int getNumCars() {
        return numCars;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void addCars(int num) {
        numCars += num;
        numAvail += num;
    }

    public boolean deleteCars(int num) {
        if (numAvail - num < 0)
            return false;
        numCars -= num;
        numAvail -= num;
        return true;
    }

    public boolean addResv() {
        if (numAvail >= 1) {
            numAvail--;
            return true;
        }
        return false;
    }

    public void cancelResv() {
        numAvail++;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String[] getColumnValues() {
        return new String[0];
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        return null;
    }

    @Override
    public Object getKey() {
        return location;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void delete() {
        isDeleted = true;
    }

    @Override
    public Object clone() {
        return new Car(location, price, numCars);
    }
}
