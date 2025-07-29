package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubRankingDTO;
import com.neocamp.api_futebol.dtos.response.MatchesResponseDTO;
import com.neocamp.api_futebol.dtos.response.MatchesRetrospectDTO;
import com.neocamp.api_futebol.dtos.response.OppRetrospectDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.NotFoundException;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {
    private static final String clubNotFoundMessage = "Clube não encontrado!";

    private final MatchRepository matchRepository;

    private final MatchValidationsService matchValidationsService;

    private final ClubRepository clubRepository;

    private final StadiumRepository stadiumRepository;

    public MatchService(MatchRepository matchRepository, MatchValidationsService matchValidationsService,
                        ClubRepository clubRepository, StadiumRepository stadiumRepository) {
        this.matchRepository = matchRepository;
        this.matchValidationsService = matchValidationsService;
        this.clubRepository = clubRepository;
        this.stadiumRepository = stadiumRepository;
    }

    public MatchesResponseDTO createMatch(@Valid MatchesRequestDTO matchesRequestDTO) {
        Club homeClub = matchValidationsService.findClubOrThrow(matchesRequestDTO.homeClubId());
        Club awayClub =  matchValidationsService.findClubOrThrow(matchesRequestDTO.awayClubId());
        Stadium stadium = matchValidationsService.findStadiumOrThrow(matchesRequestDTO.stadiumId());

        matchValidationsService.validateNotSameClubs(homeClub, awayClub);
        matchValidationsService.validateClubsActive(homeClub, awayClub);
        matchValidationsService.validateDateAfterFoundation(matchesRequestDTO.matchDateTime(), homeClub, awayClub);
        matchValidationsService.validateNoNearMatches(homeClub, awayClub, matchesRequestDTO.matchDateTime());
        matchValidationsService.validateStadiumAvailable(stadium, matchesRequestDTO.matchDateTime());

        Match match = new Match(homeClub, awayClub, stadium, matchesRequestDTO.matchDateTime(), matchesRequestDTO.homeGoals(), matchesRequestDTO.awayGoals());
        matchRepository.save(match);

        String result = formatResult(match);
        String winner = determineWinner(match);

        return new MatchesResponseDTO(
                match.getId(),
                match.getHomeClub().getName(),
                match.getAwayClub().getName(),
                match.getStadium().getName(),
                match.getMatchDateTime(),
                result,
                winner
                );
    }

    public MatchesResponseDTO updateMatch(Long id, MatchesRequestDTO matchesRequestDTO) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada!"));

        Club homeClub = matchValidationsService.findClubOrThrow(matchesRequestDTO.homeClubId());
        Club awayClub =  matchValidationsService.findClubOrThrow(matchesRequestDTO.awayClubId());
        Stadium stadium = matchValidationsService.findStadiumOrThrow(matchesRequestDTO.stadiumId());

        matchValidationsService.validateNotSameClubs(homeClub, awayClub);
        matchValidationsService.validateClubsActive(homeClub, awayClub);
        matchValidationsService.validateDateAfterFoundation(matchesRequestDTO.matchDateTime(), homeClub, awayClub);
        matchValidationsService.validateNoNearMatches(homeClub, awayClub, matchesRequestDTO.matchDateTime(), id);
        matchValidationsService.validateStadiumAvailable(stadium, matchesRequestDTO.matchDateTime(), id);

        match.setHomeClub(homeClub);
        match.setAwayClub(awayClub);
        match.setStadium(stadium);

        matchRepository.save(match);

        String result = formatResult(match);
        String winner = determineWinner(match);

        return new MatchesResponseDTO(match.getId(),
                match.getHomeClub().getName(),
                match.getAwayClub().getName(),
                match.getStadium().getName(),
                match.getMatchDateTime(),
                result,
                winner
        );
    }

    public void deleteMatch(Long id) {
        if (!matchRepository.existsById(id)) {
            throw new NotFoundException("Partida não encontrada!");
        }
        matchRepository.deleteById(id);
    }

    public MatchesResponseDTO findById(Long id) {
        var match = matchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada!"));

        String result = formatResult(match);
        String winner = determineWinner(match);

        return new MatchesResponseDTO(
                match.getId(),
                match.getHomeClub().getName(),
                match.getAwayClub().getName(),
                match.getStadium().getName(),
                match.getMatchDateTime(),
                result,
                winner
        );
    }

    public Page<MatchesResponseDTO> searchMatches(Long clubId, Long stadiumId, Boolean routs, String side, Pageable pageable) {
        if (clubId != null && !clubRepository.existsById(clubId)) {
            throw new NotFoundException(clubNotFoundMessage);
        }
        if (stadiumId != null && !stadiumRepository.existsById(stadiumId)) {
            throw new NotFoundException("Estádio não encontrado!");
        }
        if (side != null && !side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }
        Page<Match> matches = matchRepository.findWithFilters(clubId, stadiumId, routs, side, pageable);
        return matches.map(m -> {
            String result = formatResult(m);
            String winner = determineWinner(m);
            return new MatchesResponseDTO(m, result, winner);
        });
    }

    public MatchesRetrospectDTO getClubRetrospective(Long id, String side) {
        clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(clubNotFoundMessage));

        List<Match> matches;

        if (side != null && !side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }

        if ("casa".equalsIgnoreCase(side)) {
            matches = matchRepository.findAllHomeMatchesForClub(id);
        } else if ("fora".equalsIgnoreCase(side)) {
            matches = matchRepository.findAllAwayMatchesForClub(id);
        } else {
            matches = matchRepository.findAllMatchesForClub(id);
        }

        Optional<Club> value = clubRepository.findById(id);
        if (!value.isPresent()) {
            throw new NotFoundException(clubNotFoundMessage);
        }
        String clubName = value.get().getName();

        int matchesQuantity = 0;
        int victories = 0;
        int draws = 0;
        int defeats = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;

        for (Match match : matches) {
            boolean isHome = match.getHomeClub().getId().equals(id);

            // se for o time de casa pega os gols do time de casa, se for de fora pega os gols do time de fora
            int clubGoals = isHome ? match.getHomeGoals() : match.getAwayGoals();
            int oppGoals = isHome ? match.getAwayGoals() : match.getHomeGoals();

            goalsFor += clubGoals;
            goalsAgainst += oppGoals;

            if(clubGoals > oppGoals){
                victories++;
            } else if(oppGoals == clubGoals){
                draws++;
            } else defeats++;

            matchesQuantity++;
        }
        return new MatchesRetrospectDTO(clubName, matchesQuantity, victories, draws, defeats, goalsFor, goalsAgainst);
    }

    public List<OppRetrospectDTO> getOppRetrospects(Long id, String side) {
        clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(clubNotFoundMessage));

        if (side != null &&!side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }

        return matchRepository.findOppsStats(id, side);
    }

    public OppRetrospectDTO getOneOppRestrospect(Long id, Long oppId, String side) {
        clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(clubNotFoundMessage));

        clubRepository.findById(oppId)
                .orElseThrow(() -> new NotFoundException("Adversário não encontrado!"));


        Optional<Club> value = clubRepository.findById(oppId);
        if (value.isEmpty()) {
            throw new NotFoundException("Adversário não encontrado!");
        }
        String oppName = value.get().getName();
        if (side != null &&!side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }
        List<Match> matches = matchRepository.findAllMatchesBetweenClubs(id, oppId, side);

        Long matchesQuantity = 0L;
        Long victories = 0L;
        Long draws = 0L;
        Long defeats = 0L;
        Long goalsFor = 0L;
        Long goalsAgainst = 0L;

        for (Match match : matches) {
            boolean isHome = match.getHomeClub().getId().equals(id);

            int clubGoals = isHome? match.getHomeGoals() : match.getAwayGoals();
            int awayGoals = isHome? match.getAwayGoals() : match.getHomeGoals();

            goalsFor += clubGoals;
            goalsAgainst += awayGoals;

            if(clubGoals > awayGoals){
                victories++;
            } else if(awayGoals == clubGoals){
                draws++;
            }  else defeats++;

            matchesQuantity++;
        }

        return new OppRetrospectDTO(oppId, oppName, matchesQuantity, victories, draws, defeats, goalsFor, goalsAgainst);
    }

    public List<ClubRankingDTO> rankClubsByFilter(String filter) {
        List<ClubRankingDTO> ranking = matchRepository.findClubRanking();

        return switch(filter.toLowerCase()){
            case "pontos" -> ranking.stream()
                    .filter(r -> r.points() != null && r.points() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::points).reversed())
                    .toList();
            case "gols"  -> ranking.stream()
                    .filter(r -> r.goals() != null && r.goals() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::goals).reversed())
                    .toList();
            case "vitorias" ->  ranking.stream()
                    .filter(r -> r.victories() != null && r.victories() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::victories).reversed())
                    .toList();
            case "partidas" -> ranking.stream()
                    .filter(r -> r.matches() != null && r.matches() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::matches).reversed())
                    .toList();
            default -> throw new BadRequestException("Filtro inválido!");
        };
    }

        public List<ClubRankingDTO> rankClubsByFilterStream(String filter) {
            List<Club> clubs = clubRepository.findAll();
            List<Match> matches = matchRepository.findAll();

            List<ClubRankingDTO> ranking = clubs.stream()
                    .map(club -> {
                long victories = matches.stream()
                        .filter(m -> (m.getHomeClub().getId().equals(club.getId()) && m.getHomeGoals() > m.getAwayGoals()) ||
                                     (m.getAwayClub().getId().equals(club.getId()) && m.getAwayGoals() > m.getHomeGoals()))
                        .count();
                long draws = matches.stream()
                        .filter(m -> (m.getHomeClub().getId().equals(club.getId()) || m.getAwayClub().getId().equals(club.getId())) &&
                                     m.getHomeGoals().equals(m.getAwayGoals()))
                        .count();
                long points = victories * 3 + draws;
                long goals = matches.stream()
                        .mapToLong(m -> {
                            if (m.getHomeClub().getId().equals(club.getId())) return m.getHomeGoals();
                            if (m.getAwayClub().getId().equals(club.getId())) return m.getAwayGoals();
                            return 0;
                        }).sum();
                long totalMatches = matches.stream()
                        .filter(m -> m.getHomeClub().getId().equals(club.getId()) || m.getAwayClub().getId().equals(club.getId()))
                        .count();
                return new ClubRankingDTO(club.getId(), club.getName(), points, goals, victories, totalMatches);
            }).toList();

            return switch (filter) {
                case "pontos" -> ranking.stream()
                        .filter(r -> r.points() != null && r.points() > 0)
                        .sorted(Comparator.comparing(ClubRankingDTO::points).reversed())
                        .toList();
                case "gols" -> ranking.stream()
                        .filter(r -> r.goals() != null && r.goals() > 0)
                        .sorted(Comparator.comparing(ClubRankingDTO::goals).reversed())
                        .toList();
                case "vitorias" -> ranking.stream()
                        .filter(r -> r.victories() != null && r.victories() > 0)
                        .sorted(Comparator.comparing(ClubRankingDTO::victories).reversed())
                        .toList();
                case "partidas" -> ranking.stream()
                        .filter(r -> r.matches() != null && r.matches() > 0)
                        .sorted(Comparator.comparing(ClubRankingDTO::matches).reversed())
                        .toList();
                default -> throw new BadRequestException("Filtro inválido!");
            };
        }

    public String determineWinner(Match match) {
        if(match.getHomeGoals() > match.getAwayGoals()){
            return match.getHomeClub().getName();
        } else if(match.getAwayGoals() > match.getHomeGoals()){
            return match.getAwayClub().getName();
        } else {
            return "Empate";
        }
    }

    public String formatResult(Match match){
        return match.getHomeGoals() + " x " +  match.getAwayGoals();
    }
}
