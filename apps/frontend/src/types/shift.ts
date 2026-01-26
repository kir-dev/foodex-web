export interface ShiftEntity {
  id: number; // corresponds to Int
  cookingClubId: number;
  maxMembers: number;
  opening: string; // LocalDateTime → ISO string
  closing: string; // LocalDateTime → ISO string
  place: string;
  members: number[]; // List<Int>
  newbies: number[]; // List<Int>
  comment: string;
}
