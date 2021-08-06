package transaction.entity;

import transaction.InvalidIndexException;

import java.io.Serializable;

public class Hotel implements ResourceItem, Serializable {
    private String location;
    private int price;
    private int numRooms;
    private int numAvail;
    private boolean isDeleted;

    public Hotel(String location, int price, int numRooms) {
        this.location = location;
        this.price = price;
        this.numRooms = numRooms;
        this.numAvail = numRooms;
        this.isDeleted = false;
    }

    public String getLocation() {
        return location;
    }

    public int getPrice() {
        return price;
    }

    public int getNumRooms() {
        return numRooms;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void addRooms(int num) {
        numRooms += num;
        numAvail += num;
    }

    public boolean deleteRooms(int num) {
        if (numAvail - num < 0)
            return false;
        numRooms -= num;
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
        return new Hotel(location, price, numRooms);
    }
}
