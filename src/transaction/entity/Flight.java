package transaction.entity;

import transaction.InvalidIndexException;
import transaction.ResourceItem;

public class Flight implements ResourceItem {
    private String flightNum;
    private int price;
    private int numSeats;
    private int numAvail;
    private boolean isDeleted;

    public Flight(String flightNum, int price, int numSeats) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numSeats;
        this.isDeleted = false;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public int getPrice() {
        return price;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void addSeats(int num) {
        numSeats += num;
        numAvail += num;
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
        return flightNum;
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
        return new Flight(flightNum, price, numSeats);
    }
}
