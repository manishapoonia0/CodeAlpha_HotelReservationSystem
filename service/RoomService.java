package service;

import model.Room;
import model.RoomType;
import util.FileIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the hotel's room inventory.
 * Rooms are pre-seeded; availability is persisted via FileIO.
 */
public class RoomService {

    private final List<Room> rooms = new ArrayList<>();

    public RoomService() {
        seedRooms();
        restoreAvailability();
    }

    // ── Seed 20 rooms across 4 floors ─────────────────────────────────────────
    private void seedRooms() {
        // Floor 1 — Standard (101–104)
        for (int i = 1; i <= 4; i++)
            rooms.add(new Room("10" + i, RoomType.STANDARD, 1));

        // Floor 2 — Standard (201–202) + Deluxe (203–206)
        for (int i = 1; i <= 2; i++)
            rooms.add(new Room("20" + i, RoomType.STANDARD, 2));
        for (int i = 3; i <= 6; i++)
            rooms.add(new Room("20" + i, RoomType.DELUXE, 2));

        // Floor 3 — Deluxe (301–303) + Suite (304–306)
        for (int i = 1; i <= 3; i++)
            rooms.add(new Room("30" + i, RoomType.DELUXE, 3));
        for (int i = 4; i <= 6; i++)
            rooms.add(new Room("30" + i, RoomType.SUITE, 3));

        // Floor 4 — Suite (401–402) + Presidential (403–404)
        rooms.add(new Room("401", RoomType.SUITE, 4));
        rooms.add(new Room("402", RoomType.SUITE, 4));
        rooms.add(new Room("403", RoomType.PRESIDENTIAL, 4));
        rooms.add(new Room("404", RoomType.PRESIDENTIAL, 4));
    }

    private void restoreAvailability() {
        Map<String, Boolean> saved = FileIO.loadRoomAvailability();
        for (Room r : rooms) {
            if (saved.containsKey(r.getRoomNumber()))
                r.setAvailable(saved.get(r.getRoomNumber()));
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Room> getAllRooms() {
        return rooms;
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable).collect(Collectors.toList());
    }

    public List<Room> searchByType(RoomType type) {
        return rooms.stream()
                .filter(r -> r.getType() == type && r.isAvailable())
                .collect(Collectors.toList());
    }

    public Optional<Room> findByNumber(String roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst();
    }

    public void markBooked(String roomNumber) {
        findByNumber(roomNumber).ifPresent(r -> {
            r.setAvailable(false);
            FileIO.saveRoomAvailability(rooms);
        });
    }

    public void markAvailable(String roomNumber) {
        findByNumber(roomNumber).ifPresent(r -> {
            r.setAvailable(true);
            FileIO.saveRoomAvailability(rooms);
        });
    }

    public void printRoomInventory() {
        System.out.println("\n  ── ROOM INVENTORY ──────────────────────────────────────────────");
        for (RoomType type : RoomType.values()) {
            List<Room> byType = rooms.stream()
                    .filter(r -> r.getType() == type).collect(Collectors.toList());
            long avail = byType.stream().filter(Room::isAvailable).count();
            System.out.printf("  %-22s  %d rooms  (%d available)  ₹%,.0f/night%n",
                    type.getDisplayName(), byType.size(), avail, type.getPricePerNight());
        }
        System.out.println("  ────────────────────────────────────────────────────────────────");
    }

    public void printAvailableRooms(RoomType filter) {
        List<Room> list = (filter == null) ? getAvailableRooms() : searchByType(filter);
        if (list.isEmpty()) {
            System.out.println("  No available rooms" + (filter != null ? " of type " + filter : "") + ".");
            return;
        }
        System.out.println("\n  ── AVAILABLE ROOMS ─────────────────────────────────────────────");
        list.forEach(r -> System.out.println("  " + r));
        System.out.println("  ────────────────────────────────────────────────────────────────");
    }
}
