package it.uniba.di.sms2021.managerapp.enitities.file;

import java.util.Objects;

public abstract class AbstractManagerFile implements ManagerFile {
    private final String name;
    private final String type;
    private final Long size;
    private final Long lastUpdateTime;

    public AbstractManagerFile(String name, String type, Long size, Long lastUpdateTime) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutExtension() {
        return name.replaceAll("[.].{2,4}$", "");
    }

    public String getType() {
        return type;
    }

    public Long getSize() {
        return size;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractManagerFile that = (AbstractManagerFile) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(size, that.size) &&
                Objects.equals(lastUpdateTime, that.lastUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, size, lastUpdateTime);
    }

    @Override
    public String toString() {
        return "AbstractManagerFile{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", lastUpdateTime=" + lastUpdateTime +
                '}';
    }
}
