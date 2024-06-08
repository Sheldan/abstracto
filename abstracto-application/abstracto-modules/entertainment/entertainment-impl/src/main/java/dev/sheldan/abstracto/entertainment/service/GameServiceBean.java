package dev.sheldan.abstracto.entertainment.service;

import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.entertainment.exception.InvalidGameBoardException;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoard;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoardField;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoardPayload;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoardRow;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;
import dev.sheldan.abstracto.entertainment.service.management.EconomyUserManagementService;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.entertainment.config.GamesFeatureConfig.MINES_CREDITS_FACTOR;
import static dev.sheldan.abstracto.entertainment.config.GamesFeatureConfig.MINES_MINIMUM_MINES_RATIO;

@Component
public class GameServiceBean implements GameService {

    public static final String MINES_BUTTON_ORIGIN = "MINES_BUTTON";
    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private EconomyService economyService;

    @Autowired
    private EconomyUserManagementService economyUserManagementService;

    @Autowired
    private ConfigService configService;

    @Override
    public MineBoard createBoard(Integer width, Integer height, Integer mines, Long serverId) {
        double minMinesRatio = configService.getDoubleValueOrConfigDefault(MINES_MINIMUM_MINES_RATIO, serverId);
        if(mines >= width * height || width > 5 || height > 5 || mines <= 1 || height <= 1 || width <= 1) {
            throw new InvalidGameBoardException(minMinesRatio);
        }
        if((double) mines / (width * height) < minMinesRatio) {
            throw new InvalidGameBoardException(minMinesRatio);
        }
        MineBoard mineBoard = generateEmptyBoard(width, height);
        mineBoard.setMineCount(mines);
        fillWithMines(mineBoard);
        evaluateCounters(mineBoard);

        return mineBoard;
    }

    @Override
    @Transactional
    public void persistMineBoardMessage(MineBoard mineBoard, Message message, Long serverId) {
        mineBoard.setMessageId(message.getIdLong());
        mineBoard.setChannelId(message.getChannel().getIdLong());

        AServer server;
        if(serverId != null) {
            server = serverManagementService.loadServer(serverId);
        } else {
            server = null;
        }
        mineBoard.getFields().forEach(mineBoardField -> {
            MineBoardPayload payload = MineBoardPayload
                    .builder()
                    .x(mineBoardField.getX())
                    .y(mineBoardField.getY())
                    .mineBoard(mineBoard)
                    .build();
            String componentId = mineBoard.getBoardId() + "_" + mineBoardField.getX() + "_" + mineBoardField.getY();
            componentPayloadService.createButtonPayload(componentId, payload, MINES_BUTTON_ORIGIN, server);
        });
    }

    @Override
    @Transactional
    public void updateMineBoard(MineBoard mineBoard) {
        mineBoard.getFields().forEach(mineBoardField -> {
            MineBoardPayload newPayload = MineBoardPayload
                    .builder()
                    .x(mineBoardField.getX())
                    .y(mineBoardField.getY())
                    .mineBoard(mineBoard)
                    .build();
            String componentId = mineBoard.getBoardId() + "_" + mineBoardField.getX() + "_" + mineBoardField.getY();
            componentPayloadService.updateButtonPayload(componentId, newPayload);
        });
    }

    @Override
    public void uncoverBoard(MineBoard mineBoard) {
        mineBoard.getFields().forEach(mineBoardField -> {
            if(mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.COVERED)) {
                mineBoardField.setType(MineBoardField.MineBoardFieldType.UNCOVERED);
            }
        });
    }

    @Override
    public void evaluateCreditChanges(MineBoard mineBoard) {
        Long credits = mineBoard.getCredits().longValue();
        Integer mineCount = mineBoard.getMineCount();
        List<MineBoardField> allFields = mineBoard.getFields();
        Integer leftFields = (int) allFields
                .stream()
                .filter(mineBoardField -> mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.COVERED)
                        || mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.MINE))
                .count();
        Integer fieldCount = allFields.size();
        Integer uncoveredFields = fieldCount - leftFields;
        Long creditChange = (long) (calculateCreditFactor(fieldCount, mineCount, uncoveredFields) * credits);
        if(mineBoard.getState().equals(MineResult.WON)) {
            Double factor = configService.getDoubleValueOrConfigDefault(MINES_CREDITS_FACTOR, mineBoard.getServerId());
            creditChange = (long) (creditChange * factor);
        }
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(mineBoard.getServerId())
                .userId(mineBoard.getUserId())
                .build();
        Optional<EconomyUser> economyUserOptional = economyUserManagementService.getUser(serverUser);
        if(economyUserOptional.isPresent()) {
            economyService.addCredits(economyUserOptional.get(), -credits);
            economyService.addCredits(economyUserOptional.get(), creditChange);
        }
        mineBoard.setCreditChange(creditChange);
    }

    private double calculateCreditFactor(int totalFields, int mines, int uncovered) {
        return ((double) uncovered / (totalFields - mines)) + totalFields / 25.0 - ((totalFields - mines) / 25.0);
    }

    @Override
    public MineResult uncoverField(MineBoard board, Integer x, Integer y) {
        return uncoverFieldOnBoard(board, x, y);
    }

    public GameService.MineResult uncoverFieldOnBoard(MineBoard board, Integer x, Integer y) {
        MineBoardField field = board.getField(x, y);
        if(!MineBoardField.canInteract(field.getType())) {
            return GameService.MineResult.CONTINUE;
        }
        if(field.getType().equals(MineBoardField.MineBoardFieldType.MINE)) {
            field.setType(MineBoardField.MineBoardFieldType.EXPLODED);
            return GameService.MineResult.LOST;
        }
        if(field.getType().equals(MineBoardField.MineBoardFieldType.COVERED)) {
            if(field.getCounterValue() == 0) {
                Set<String> alreadyConsidered = new HashSet<>();
                Queue<MineBoardField> toUncover = new LinkedList<>();
                toUncover.add(field);
                while(!toUncover.isEmpty()) {
                    MineBoardField fieldToHandle = toUncover.poll();
                    fieldToHandle.setType(MineBoardField.MineBoardFieldType.UNCOVERED);
                    alreadyConsidered.add(fieldToHandle.getX() + "_" + fieldToHandle.getY());
                    // only when we actually got a free field, we should add its neighbors to the next one to uncover
                    if(fieldToHandle.getCounterValue() == 0) {
                        List<MineBoardField> neighbors = getNeighbors(board, fieldToHandle.getX(), fieldToHandle.getY(), false);
                        List<MineBoardField> uncoverableNeighbors = neighbors
                                .stream().filter(mineBoardField -> mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.COVERED))
                                .collect(Collectors.toList());
                        uncoverableNeighbors.forEach(mineBoardField -> {
                            if(!alreadyConsidered.contains(mineBoardField.getX() + "_" + mineBoardField.getY())) {
                                mineBoardField.setType(MineBoardField.MineBoardFieldType.UNCOVERED);
                                // only if t he newly found neighbor is a free field, we should discover its neighbors
                                if(mineBoardField.getCounterValue() == 0) {
                                    toUncover.addAll(uncoverableNeighbors);
                                }
                            }
                        });
                    }
                }
            } else {
                field.setType(MineBoardField.MineBoardFieldType.UNCOVERED);
            }
            if(hasWon(board)) {
                return GameService.MineResult.WON;
            }
            return GameService.MineResult.CONTINUE;
        }
        throw new IllegalStateException("Did not find correct type of field.");
    }

    private List<MineBoardField> getNeighbors(MineBoard mineBoard, int xPosition, int yPosition) {
        return getNeighbors(mineBoard, xPosition, yPosition, false);
    }

    private List<MineBoardField> getNeighbors(MineBoard board, int xPosition, int yPosition, boolean directOnly) {
        List<MineBoardField> neighbors = new ArrayList<>();
        boolean isFirstRow = yPosition == 0;
        boolean isLastRow = yPosition == board.getRowCount() - 1;
        boolean isFirstColumn = xPosition == 0;
        boolean isLastColumn = xPosition == board.getColumnCount() - 1;
        if(!isFirstColumn) {
            if(!isFirstRow && !directOnly) {
                neighbors.add(board.getField(xPosition - 1, yPosition - 1));
            }
            neighbors.add(board.getField(xPosition - 1, yPosition));
            if(!isLastRow && !directOnly) {
                neighbors.add(board.getField(xPosition - 1, yPosition + 1));
            }
        }
        if(!isFirstRow && !directOnly) {
            neighbors.add(board.getField(xPosition, yPosition - 1));
        }
        if(!isLastRow && !directOnly) {
            neighbors.add(board.getField(xPosition, yPosition + 1));
        }
        if(!isLastColumn) {
            if(!isFirstRow && !directOnly) {
                neighbors.add(board.getField(xPosition + 1, yPosition - 1));
            }
            neighbors.add(board.getField(xPosition + 1, yPosition));
            if(!isLastRow && !directOnly) {
                neighbors.add(board.getField(xPosition + 1, yPosition + 1));
            }
        }
        return neighbors;
    }


    public MineBoard generateEmptyBoard(Integer width, Integer height) {
        List<MineBoardRow> rows = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            List<MineBoardField> fields = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                MineBoardField field = MineBoardField
                        .builder()
                        .y(y)
                        .x(x)
                        .counterValue(0)
                        .type(MineBoardField.MineBoardFieldType.COVERED)
                        .build();
                fields.add(field);
            }
            MineBoardRow row = MineBoardRow
                    .builder()
                    .fields(fields)
                    .build();
            rows.add(row);
        }
        return MineBoard
                .builder()
                .rows(rows)
                .state(MineResult.CONTINUE)
                .columnCount(width)
                .creditChange(0L)
                .rowCount(height)
                .boardId(UUID.randomUUID().toString())
                .build();
    }

    public void fillWithMines(MineBoard board) {
        Set<String> usedKeys = new HashSet<>();
        int maxIterations = 1_0000_000;
        int iterations = 0;
        List<Pair<Integer, Integer>> foundPositions = new ArrayList<>();
        do  {
            int x = secureRandom.nextInt(board.getColumnCount());
            int y = secureRandom.nextInt(board.getRowCount());
            String positionKey = x + "_" + y;
            if(!usedKeys.contains(positionKey)) {
                foundPositions.add(Pair.of(x, y));
                usedKeys.add(positionKey);
                iterations = 0;
            }
            iterations++;
        } while(foundPositions.size() < board.getMineCount() && iterations < maxIterations);
        foundPositions.forEach(xYPair -> board.getRows().get(xYPair.getRight()).getFields().get(xYPair.getLeft()).setType(MineBoardField.MineBoardFieldType.MINE));
    }

    public void evaluateCounters(MineBoard board) {
        board.getRows().forEach(mineBoardRow -> mineBoardRow.getFields().forEach(mineBoardField -> {
            if(!mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.MINE)) {
                long mineCounts = getMineCounts(board, mineBoardField.getX(), mineBoardField.getY());
                mineBoardField.setCounterValue((int) mineCounts);
            }
        }));
    }

    private long getMineCounts(MineBoard board, int xPosition, int yPosition) {
        List<MineBoardField> neighbors = getNeighbors(board, xPosition, yPosition);
        return neighbors
                .stream()
                .filter(mineBoardField -> mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.MINE))
                .count();
    }

    private boolean hasWon(MineBoard board) {
        return board
                .getFields()
                .stream()
                .noneMatch(mineBoardField ->
                        mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.COVERED)
                        && !mineBoardField.getType().equals(MineBoardField.MineBoardFieldType.MINE));
    }

}
