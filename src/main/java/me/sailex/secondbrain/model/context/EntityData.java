package me.sailex.secondbrain.model.context;

public record EntityData(int id, String name, boolean isPlayer) {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        EntityData other = (EntityData) obj;
        return other.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
