package itzglecni.customhitboxes.config;

public class ModConfig {
    public boolean enabled = true;
    public boolean renderEntities = false;
    public boolean renderOnlyWhenElytraEquipped = false;
    public boolean renderProjectiles = true;
    public boolean lookingDirection = false;
    public boolean removeDirectionArrow = true;
    public boolean foreheadLine = true;
    public boolean selfHitbox = false;
    public boolean hoverColorHitbox = true;
    public boolean hitColorHitbox = true;
    public boolean noFireworkHitbox = true;
    public boolean noStuckArrows = true;
    public boolean openConfigHotkeyEnabled = true;

    public float hitboxWidth = 2.5f;
    public float nonPlayerHitboxWidth = 2.5f;

    public int hitboxMainColor = 0xFFFFFF;
    public boolean rainbowMain = false;
    
    public int hoverColor = 0x00FF00;
    public boolean rainbowHover = false;
    
    public int hitColor = 0xFF0000;
    public boolean rainbowHit = false;
    
    public int lookingDirectionColor = 0x0000FF;
    public boolean rainbowLooking = false;
    
    public int foreheadLineColor = 0x00FFFF;
    public boolean rainbowForehead = false;
    
    public int projectilesColor = 0x0000FF;
    public boolean rainbowProjectiles = false;

    public ModConfig copy() {
        ModConfig out = new ModConfig();
        out.copyFrom(this);
        return out;
    }

    public void copyFrom(ModConfig other) {
        this.enabled = other.enabled;
        this.renderEntities = other.renderEntities;
        this.renderOnlyWhenElytraEquipped = other.renderOnlyWhenElytraEquipped;
        this.renderProjectiles = other.renderProjectiles;
        this.lookingDirection = other.lookingDirection;
        this.removeDirectionArrow = other.removeDirectionArrow;
        this.foreheadLine = other.foreheadLine;
        this.selfHitbox = other.selfHitbox;
        this.hoverColorHitbox = other.hoverColorHitbox;
        this.hitColorHitbox = other.hitColorHitbox;
        this.noFireworkHitbox = other.noFireworkHitbox;
        this.noStuckArrows = other.noStuckArrows;
        this.openConfigHotkeyEnabled = other.openConfigHotkeyEnabled;
        this.hitboxWidth = other.hitboxWidth;
        this.nonPlayerHitboxWidth = other.nonPlayerHitboxWidth;
        this.hitboxMainColor = other.hitboxMainColor;
        this.rainbowMain = other.rainbowMain;
        this.hoverColor = other.hoverColor;
        this.rainbowHover = other.rainbowHover;
        this.hitColor = other.hitColor;
        this.rainbowHit = other.rainbowHit;
        this.lookingDirectionColor = other.lookingDirectionColor;
        this.rainbowLooking = other.rainbowLooking;
        this.foreheadLineColor = other.foreheadLineColor;
        this.rainbowForehead = other.rainbowForehead;
        this.projectilesColor = other.projectilesColor;
        this.rainbowProjectiles = other.rainbowProjectiles;
    }
}