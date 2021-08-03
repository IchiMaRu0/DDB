package transaction.entity;

import transaction.InvalidIndexException;
import transaction.ResourceItem;

public class Customer implements ResourceItem {
    private String customerName;
    private boolean isDeleted;

    public Customer(String customerName){
        this.customerName=customerName;
        this.isDeleted=false;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Customer() {
        super();
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
        return null;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void delete() {
        isDeleted=true;
    }

    @Override
    public Object clone() {
        return new Customer(customerName);
    }
}
