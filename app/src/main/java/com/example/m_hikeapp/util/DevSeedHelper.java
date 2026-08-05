package com.example.m_hikeapp.util;

import android.content.Context;

import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;
import com.example.m_hikeapp.repository.HikeRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Developer helper to automatically populate sample data when the database is empty.
 */
public class DevSeedHelper {

    public static void seedIfEmpty(Context context, Runnable onComplete) {
        HikeRepository repository = HikeRepository.getInstance(context);

        repository.getAllHikes(existing -> {
            if (existing != null && !existing.isEmpty()) {
                if (onComplete != null) onComplete.run();
                return;
            }

            // Seed 8 realistic Hikes
            List<Hike> sampleHikes = createSampleHikes();
            insertHikesSequentially(repository, sampleHikes, 0, onComplete);
        });
    }

    private static void insertHikesSequentially(HikeRepository repository, List<Hike> hikes, int index, Runnable onComplete) {
        if (index >= hikes.size()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Hike hike = hikes.get(index);
        repository.addHike(hike, (success, newIdStr) -> {
            if (success) {
                try {
                    long newHikeId = Long.parseLong(newIdStr);
                    List<Observation> observations = createObservationsForIndex(index, newHikeId);
                    insertObservationsSequentially(repository, observations, 0, () ->
                            insertHikesSequentially(repository, hikes, index + 1, onComplete));
                } catch (Exception e) {
                    insertHikesSequentially(repository, hikes, index + 1, onComplete);
                }
            } else {
                insertHikesSequentially(repository, hikes, index + 1, onComplete);
            }
        });
    }

    private static void insertObservationsSequentially(HikeRepository repository, List<Observation> obsList, int index, Runnable onDone) {
        if (index >= obsList.size()) {
            if (onDone != null) onDone.run();
            return;
        }

        repository.addObservation(obsList.get(index), (success, msg) ->
                insertObservationsSequentially(repository, obsList, index + 1, onDone));
    }

    private static List<Hike> createSampleHikes() {
        List<Hike> list = new ArrayList<>();

        // 1. Hoan Kiem Lake
        Hike h1 = new Hike("Hoan Kiem Lake Morning Walk", "Hoan Kiem District, Hanoi", "2025-03-15", true, 2.5, "Easy");
        h1.setDescription("A peaceful morning walk around Hoan Kiem Lake. Scenic views of Turtle Tower and Ngoc Son Temple. Very popular with locals doing Tai Chi.");
        h1.setCustomField1("Best time: 6:00 - 8:00 AM");
        h1.setCustomField2("Bring water and sunscreen");
        h1.setLatitude(21.0285);
        h1.setLongitude(105.8516);
        h1.setEstimatedDurationMin(30);
        h1.setActualDurationMin(35);
        h1.setWeatherCondition("Sunny");
        h1.setWeatherNotes("Clear sky, 26°C, light breeze.");
        h1.setTrailRating(5);
        h1.setTrailNotes("Excellent paved path all the way around. Clean and well-maintained.");
        list.add(h1);

        // 2. Ba Vi National Park
        Hike h2 = new Hike("Ba Vi Peak Trail", "Ba Vi National Park, Ha Noi", "2025-04-20", true, 8.0, "Moderate");
        h2.setDescription("A forest trail climbing through dense subtropical forest to the French-era ruins at 1,227m. Misty conditions at the top with views across the Red River Delta.");
        h2.setCustomField1("Entry fee: 60,000 VND");
        h2.setCustomField2("Slippery in rain — wear grip shoes");
        h2.setLatitude(21.0694);
        h2.setLongitude(105.3637);
        h2.setEstimatedDurationMin(125);
        h2.setActualDurationMin(140);
        h2.setWeatherCondition("Partly Cloudy");
        h2.setWeatherNotes("Cool 18°C at summit, misty.");
        h2.setTrailRating(4);
        h2.setTrailNotes("Good trail markings. Stone steps at final climb.");
        list.add(h2);

        // 3. Fansipan
        Hike h3 = new Hike("Fansipan Base to Summit", "Sapa, Lao Cai Province", "2025-05-10", true, 19.0, "Hard");
        h3.setDescription("The legendary Roof of Indochina at 3,143m. Two-day trek through bamboo forest, alpine meadows and rocky summit.");
        h3.setCustomField1("Overnight camp required");
        h3.setCustomField2("Book guide in advance");
        h3.setLatitude(22.3036);
        h3.setLongitude(103.7763);
        h3.setEstimatedDurationMin(364);
        h3.setActualDurationMin(420);
        h3.setWeatherCondition("Cloudy");
        h3.setWeatherNotes("Cloud cover all day, 8°C at summit.");
        h3.setTrailRating(3);
        h3.setTrailNotes("Trail quality drops above 2800m — loose rocks.");
        list.add(h3);

        // 4. Ta Nang - Phan Dung
        Hike h4 = new Hike("Ta Nang – Phan Dung 3-Day Trek", "Binh Thuan / Lam Dong", "2025-06-05", false, 42.0, "Expert");
        h4.setDescription("Vietnam's most challenging multi-day wilderness trek. Crosses pine forests, grasslands and river crossings.");
        h4.setCustomField1("Full camping kit required");
        h4.setCustomField2("GPS essential — trail not marked");
        h4.setLatitude(11.6834);
        h4.setLongitude(108.1231);
        h4.setEstimatedDurationMin(720);
        h4.setActualDurationMin(720);
        h4.setWeatherCondition("Sunny");
        h4.setWeatherNotes("Hot 34°C in open grasslands.");
        h4.setTrailRating(2);
        h4.setTrailNotes("No formal trail — requires navigation skills.");
        list.add(h4);

        // 5. Hyde Park
        Hike h5 = new Hike("Hyde Park & Kensington Gardens", "Hyde Park, London, UK", "2025-07-12", false, 5.5, "Easy");
        h5.setDescription("A leisurely circuit through Hyde Park and Kensington Gardens. Past the Serpentine Gallery and Kensington Palace.");
        h5.setCustomField1("No parking nearby — take Tube");
        h5.setLatitude(51.5073);
        h5.setLongitude(-0.1657);
        h5.setEstimatedDurationMin(66);
        h5.setActualDurationMin(90);
        h5.setWeatherCondition("Partly Cloudy");
        h5.setWeatherNotes("Typical London 19°C, overcast with sunny spells.");
        h5.setTrailRating(4);
        h5.setTrailNotes("Excellent flat paths. Very busy on weekends.");
        list.add(h5);

        // 6. Snowdon
        Hike h6 = new Hike("Snowdon Horseshoe via PYG Track", "Snowdonia, Wales", "2025-08-03", true, 12.0, "Moderate");
        h6.setDescription("Classic Welsh mountain route taking in Crib Goch ridge and Snowdon summit (1,085m).");
        h6.setCustomField1("Pen-y-Pass car park");
        h6.setLatitude(53.0683);
        h6.setLongitude(-4.0764);
        h6.setEstimatedDurationMin(187);
        h6.setActualDurationMin(210);
        h6.setWeatherCondition("Wind");
        h6.setWeatherNotes("Strong 45km/h winds on Crib Goch ridge.");
        h6.setTrailRating(5);
        h6.setTrailNotes("Outstanding route. Scramble is exposed but well-used.");
        list.add(h6);

        // 7. Ben Nevis
        Hike h7 = new Hike("Ben Nevis via CMD Arete", "Fort William, Scotland", "2025-09-14", true, 24.0, "Hard");
        h7.setDescription("Britain's highest peak (1,345m) via the spectacular CMD Arete ridge.");
        h7.setCustomField1("Glen Nevis car park");
        h7.setLatitude(56.7969);
        h7.setLongitude(-5.0035);
        h7.setEstimatedDurationMin(364);
        h7.setActualDurationMin(390);
        h7.setWeatherCondition("Cloudy");
        h7.setWeatherNotes("Thick cloud above 900m, 4°C at summit.");
        h7.setTrailRating(4);
        h7.setTrailNotes("CMD Arete is outstanding in clear weather.");
        list.add(h7);

        // 8. Hoi An
        Hike h8 = new Hike("Hoi An Ancient Town Walk", "Hoi An, Quang Nam", "2025-10-22", false, 7.0, "Easy");
        h8.setDescription("A flat cultural walk through UNESCO-listed Hoi An Ancient Town and across the Thu Bon River.");
        h8.setCustomField1("Boat fare: 20,000 VND");
        h8.setLatitude(15.8801);
        h8.setLongitude(108.3380);
        h8.setEstimatedDurationMin(84);
        h8.setActualDurationMin(100);
        h8.setWeatherCondition("Sunny");
        h8.setWeatherNotes("Beautiful 29°C, low humidity.");
        h8.setTrailRating(5);
        h8.setTrailNotes("Flat and easy throughout. Start early.");
        list.add(h8);

        return list;
    }

    private static Observation createObs(long hikeId, String title, String obsTime, String comment, Integer stepCount, String photoUri, Double temp) {
        Observation obs = new Observation(hikeId, title, obsTime);
        obs.setComment(comment);
        obs.setStepCount(stepCount);
        obs.setPhotoUri(photoUri);
        obs.setTemperatureCelsius(temp);
        return obs;
    }

    private static List<Observation> createObservationsForIndex(int hikeIndex, long hikeId) {
        List<Observation> obs = new ArrayList<>();
        switch (hikeIndex) {
            case 0: // Hoan Kiem
                obs.add(createObs(hikeId, "Sunrise at Turtle Tower", "06:15", "Golden light reflecting off the lake. About 50 locals doing Tai Chi on the east bank.", 842, null, 24.5));
                obs.add(createObs(hikeId, "Ngoc Son Temple", "07:00", "Crossed the red Huc Bridge to visit the temple. Beautiful lotus decorations.", 2100, null, 26.0));
                obs.add(createObs(hikeId, "Street food breakfast stop", "07:45", "Banh mi and iced coffee at a corner stall near the south end. Delicious!", 3500, null, 27.2));
                break;
            case 1: // Ba Vi
                obs.add(createObs(hikeId, "Trailhead gate", "08:00", "Paid entry fee. First 2km through mature eucalyptus forest — good shade.", 0, null, 22.0));
                obs.add(createObs(hikeId, "First viewpoint at 800m", "09:30", "Cleared forest line — views across the valley to Hanoi. Cool 18.5°C.", 6200, null, 18.5));
                obs.add(createObs(hikeId, "French-era ruins", "11:00", "Eerie stone ruins of French resort at summit. Thick mist rolling in.", 11800, null, 15.0));
                break;
            case 2: // Fansipan
                obs.add(createObs(hikeId, "Tram Cat Cat departure", "05:30", "Started before dawn. Cool 14°C. Head torches needed for first 30 minutes.", 0, null, 14.0));
                obs.add(createObs(hikeId, "Camp 2 overnight", "16:00", "Reached camp at 2,800m. Temperature dropped sharply after sunset.", 24000, null, 9.0));
                obs.add(createObs(hikeId, "Summit! 3143m", "07:30", "Reached roof of Indochina! Photo at the summit marker.", 32000, null, 8.0));
                break;
            case 3: // Ta Nang
                obs.add(createObs(hikeId, "Pine forest entry", "06:00", "Entered pine forest at dawn. Ancient trees over 200 years old.", 0, null, 20.0));
                obs.add(createObs(hikeId, "River crossing", "10:30", "Waist-deep crossing. Took 25 mins to get everyone across safely with ropes.", 12000, null, 25.0));
                obs.add(createObs(hikeId, "Open grassland plateau", "14:00", "Highland savanna with rolling golden grass as far as eye can see.", 28000, null, 34.0));
                break;
            case 4: // Hyde Park
                obs.add(createObs(hikeId, "Serpentine Lake", "14:00", "Swans and ducks on the lake. Summer atmosphere, very relaxed.", 3200, null, 19.0));
                obs.add(createObs(hikeId, "Diana Memorial Fountain", "14:45", "Oval granite channel. Children paddling in lower sections.", 5800, null, 20.0));
                break;
            case 5: // Snowdon
                obs.add(createObs(hikeId, "Crib Goch ridge", "09:15", "Hands-and-feet scrambling on exposed ridge. Wind 45km/h.", 5400, null, 9.0));
                obs.add(createObs(hikeId, "Snowdon Summit 1085m", "11:30", "Made it! Summit cafe serving hot soup — exactly what was needed.", 10200, null, 9.0));
                break;
            case 6: // Ben Nevis
                obs.add(createObs(hikeId, "CMD Arete ridge", "10:00", "Narrow rocky ridge with vertical drops. Cloud swirling below.", 8900, null, 6.0));
                obs.add(createObs(hikeId, "Summit plateau in cloud", "12:30", "Thick cloud, visibility < 10m. Used compass to find trig point.", 16200, null, 4.0));
                break;
            case 7: // Hoi An
                obs.add(createObs(hikeId, "Japanese Covered Bridge", "08:30", "18th century bridge. Intricate carvings and small shrine inside.", 1400, null, 28.0));
                obs.add(createObs(hikeId, "Cam Kim Island boat", "10:00", "Wooden boat across Thu Bon River. Quiet rural island.", 4800, null, 29.5));
                break;
        }
        return obs;
    }
}
