package com.itcmdas.vo;

/**
 * @Classname Medicine
 * @Description TODO
 * @Author lbt
 * @Date 2026/3/15 16:02
 * @Version 1.0
 */
public class Medicine {
    private int id;
    private String medicine;
    private String efficacy;
    private String properties;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMedicine() {
        return medicine;
    }

    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }

    public String getEfficacy() {
        return efficacy;
    }

    public void setEfficacy(String efficacy) {
        this.efficacy = efficacy;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }


    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", medicine='" + medicine + '\'' +
                ", efficacy='" + efficacy + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}
