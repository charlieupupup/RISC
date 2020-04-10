package edu.duke.ece651.riskclient.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.duke.ece651.risk.shared.ToClientMsg.RoundInfo;
import edu.duke.ece651.risk.shared.WorldState;
import edu.duke.ece651.risk.shared.action.Action;
import edu.duke.ece651.risk.shared.map.Territory;
import edu.duke.ece651.risk.shared.map.Unit;
import edu.duke.ece651.risk.shared.map.WorldMap;
import edu.duke.ece651.risk.shared.player.Player;
import edu.duke.ece651.riskclient.R;
import edu.duke.ece651.riskclient.adapter.TerritoryAdapter;
import edu.duke.ece651.riskclient.listener.onReceiveListener;
import edu.duke.ece651.riskclient.listener.onRecvAttackResultListener;
import edu.duke.ece651.riskclient.listener.onResultListener;

import static edu.duke.ece651.risk.shared.Constant.ACTION_DONE;
import static edu.duke.ece651.risk.shared.Constant.GAME_OVER;

import static edu.duke.ece651.riskclient.Constant.ACTION_PERFORMED;
import static edu.duke.ece651.riskclient.Constant.MAP_NAME_TO_RESOURCE_ID;
import static edu.duke.ece651.riskclient.Constant.NETWORK_PROBLEM;
import static edu.duke.ece651.riskclient.RiskApplication.getPlayerID;

import static edu.duke.ece651.riskclient.RiskApplication.getRoomName;
import static edu.duke.ece651.riskclient.RiskApplication.recv;
import static edu.duke.ece651.riskclient.RiskApplication.send;
import static edu.duke.ece651.riskclient.RiskApplication.setPlayerID;
import static edu.duke.ece651.riskclient.utils.HTTPUtils.recvAttackResult;
import static edu.duke.ece651.riskclient.utils.UIUtils.showToastUI;

public class PlayGameActivity extends AppCompatActivity {
    private static final String TAG = PlayGameActivity.class.getSimpleName();

    public static final String PLAYING_MAP = "playingMap";
    public static final String FOOD_RESOURCE = "food";
    public static final String TECH_RESOURCE = "tech";
    public static final String CURRENT_TECH_LEVEL = "currentTechLevel";
    public static final String IS_MOVE = "isMove";
    public static final String IS_UPGRADE_MAX = "isUpgradeMax";

    private static final String TYPE_MOVE = "move";
    private static final String TYPE_ATTACK = "attack";
    private static final String TYPE_UPGRADE_UNIT = "upgrade unit";
    private static final String TYPE_UPGRADE_MAX = "upgrade max tech";
    private static final String TYPE_ALLIANCE = "form alliance";
    private static final String TYPE_DONE = "done";

    private static final int REQUEST_ACTION_MOVE = 1;
    private static final int REQUEST_ACTION_ATTACK = 2;
    private static final int REQUEST_ACTION_UPGRADE_MAX = 3;
    private static final int REQUEST_ACTION_UPGRADE_UNIT = 4;
    private static final int REQUEST_ACTION_ALLIANCE = 5;

    /**
     * UI variable
     */
    private TextView tvRoundNum;
    private TextView tvPlayerInfo;
    private TextView tvActionInfo;
    private Button btPerform;
    private Button btMoveAttack;
    private Button btUpgrade;
    private Button btDone;
    private ImageView imgMap;

    /**
     * Variable
     */
    private TerritoryAdapter territoryAdapter;
    private List<Action> performedActions;
    private WorldMap<String> map;
    private Player<String> player;
    private int roundNum;
    private boolean isLose;
    private boolean hasShowDialog;
    private String actionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(getRoomName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        performedActions = new ArrayList<>();
        roundNum = 1;
        isLose = false;
        hasShowDialog = false;
        actionType = TYPE_MOVE;

        setUpUI();

        // make sure user can't do anything before we receive the first round data
        setAllButtonClickable(false);

        newRound();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_ACTION_MOVE:
            case REQUEST_ACTION_ATTACK:
            case REQUEST_ACTION_UPGRADE_UNIT:
            case REQUEST_ACTION_UPGRADE_MAX:
            case REQUEST_ACTION_ALLIANCE:
                if (resultCode == RESULT_OK){
                    Action action = (Action) data.getSerializableExtra(ACTION_PERFORMED);
                    // server check that the action is valid, so we perform it to update current map
                    // NOTE: this only update the copy of the map, we will still get the latest map from server at the beginning of each term
                    action.perform(new WorldState(player, map));
                    performedActions.add(action);
                    showPerformedActions();
                    showPlayerInfo();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpUI(){
        btPerform = findViewById(R.id.bt_perform);
        btPerform.setOnClickListener(v -> {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            int requestCode = -1;
            switch (actionType){
                case TYPE_MOVE:
                    intent.setComponent(new ComponentName(PlayGameActivity.this, MoveAttackActivity.class));
                    bundle.putBoolean(IS_MOVE, true);
                    bundle.putSerializable(PLAYING_MAP, map);
                    bundle.putInt(FOOD_RESOURCE, player.getFoodNum());
                    requestCode = REQUEST_ACTION_MOVE;
                    break;
                case TYPE_ATTACK:
                    intent.setComponent(new ComponentName(PlayGameActivity.this, MoveAttackActivity.class));
                    bundle.putBoolean(IS_MOVE, false);
                    bundle.putSerializable(PLAYING_MAP, map);
                    bundle.putInt(FOOD_RESOURCE, player.getFoodNum());
                    requestCode = REQUEST_ACTION_ATTACK;
                    break;
                case TYPE_UPGRADE_UNIT:
                    intent.setComponent(new ComponentName(PlayGameActivity.this, UpgradeActivity.class));
                    bundle.putBoolean(IS_UPGRADE_MAX, false);
                    bundle.putSerializable(PLAYING_MAP, map);
                    bundle.putInt(TECH_RESOURCE, player.getTechNum());
                    bundle.putInt(CURRENT_TECH_LEVEL, player.getTechLevel());
                    requestCode = REQUEST_ACTION_UPGRADE_UNIT;
                    break;
                case TYPE_UPGRADE_MAX:
                    intent.setComponent(new ComponentName(PlayGameActivity.this, UpgradeActivity.class));
                    bundle.putBoolean(IS_UPGRADE_MAX, true);
                    bundle.putSerializable(PLAYING_MAP, map);
                    bundle.putInt(TECH_RESOURCE, player.getTechNum());
                    bundle.putInt(CURRENT_TECH_LEVEL, player.getTechLevel());
                    requestCode = REQUEST_ACTION_UPGRADE_MAX;
                    break;
                case TYPE_ALLIANCE:
                    requestCode = REQUEST_ACTION_ALLIANCE;
                    break;
                case TYPE_DONE:
                    // pop up a dialog to ask confirm
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Finish this round");
                    builder.setMessage("once you finish this round, you can't undo it");
                    builder.setPositiveButton("Finish", (dialog, which) -> {
                        roundOver();
                    });
                    builder.setNegativeButton("Cancel", ((dialog, which) -> {
                        // do nothing
                    }));
                    builder.show();
                    return;
            }
            intent.putExtras(bundle);
            startActivityForResult(intent, requestCode);
        });

        tvPlayerInfo = findViewById(R.id.tv_player_info);
        tvPlayerInfo.setText("Please wait other players to finish assigning units...");

        tvRoundNum = findViewById(R.id.tv_round_number);
        tvRoundNum.setText(String.valueOf(roundNum));

        imgMap = findViewById(R.id.img_map);

        tvActionInfo = findViewById(R.id.tv_action_info);
        tvActionInfo.setMovementMethod(new ScrollingMovementMethod());

        showPerformedActions();
        setUpActionDropdown();
        setUpTerritoryList();
    }

    /**
     * Set up the recycler view of territory list.
     */
    private void setUpTerritoryList(){
        RecyclerView rvTerritoryList = findViewById(R.id.rv_territory_list);

        territoryAdapter = new TerritoryAdapter();
        territoryAdapter.setListener(position -> {
            showTerritoryDetailDialog(territoryAdapter.getTerritory(position));
        });

        rvTerritoryList.setLayoutManager(new LinearLayoutManager(PlayGameActivity.this));
        rvTerritoryList.setHasFixedSize(true);
        rvTerritoryList.setAdapter(territoryAdapter);
    }

    /**
     * Set up the action type drop down.
     */
    private void setUpActionDropdown(){
        TextInputLayout layout = findViewById(R.id.action_dropdown);
        layout.setHint("Action Type");

        String[] items = new String[] {TYPE_MOVE, TYPE_ATTACK, TYPE_UPGRADE_UNIT, TYPE_UPGRADE_MAX, TYPE_ALLIANCE, TYPE_DONE};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        PlayGameActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        items);

        // set up default choice
        actionType = adapter.getItem(0);

        AutoCompleteTextView dropdownAction = layout.findViewById(R.id.input);
        dropdownAction.setAdapter(adapter);
        dropdownAction.setText(adapter.getItem(0), false);
        dropdownAction.setOnItemClickListener((parent, v, position, id) -> {
            actionType = adapter.getItem(position);
        });
    }

    /**
     * Show the detail information of one territory.
     * @param territory target territory
     */
    private void showTerritoryDetailDialog(Territory territory){
        // generate the detail info of one territory
        StringBuilder detailInfo = new StringBuilder();
        detailInfo.append("Resource Info:\n");
        detailInfo.append("Food yield: ").append(territory.getFoodYield()).append("\n");
        detailInfo.append("Tech yield: ").append(territory.getTechYield()).append("\n");
        detailInfo.append("Units Info:\n");
        Map<Integer, List<Unit>> units = territory.getUnitGroup();
        if (units.isEmpty()){
            detailInfo.append("no units on this territory\n");
        }
        else {
            for (Map.Entry<Integer, List<Unit>> entry : units.entrySet()){
                detailInfo.append(entry.getValue().size()).append(" units with level ").append(entry.getKey()).append("\n");
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail Info");
        builder.setMessage(detailInfo.toString());
        builder.show();
    }

    /**
     * The function will be executed at the end of each round.
     */
    private void roundOver(){
        // disable all buttons
        setAllButtonClickable(false);
        send(ACTION_DONE, new onResultListener() {
            @Override
            public void onFailure(String error) {
                showToastUI(PlayGameActivity.this, NETWORK_PROBLEM);
                setAllButtonClickable(true);
            }

            @Override
            public void onSuccessful() {
                // successfully indicate we are finish, waiting for the attack result
                receiveAttackResult();
            }
        });
    }

    /**
     * Since the requirement require send the result of each attack action to *all* players,
     * we use a while loop to keep receiving all results until OVER
     */
    private void receiveAttackResult(){
        StringBuilder results = new StringBuilder();
        recvAttackResult(new onRecvAttackResultListener() {
            @Override
            public void onNewResult(String result) {
                runOnUiThread(() -> {
                    results.append(result).append("\n");
                    tvActionInfo.setText(results.toString());
                });
            }

            @Override
            public void onOver() {
                checkGameEnd();
            }

            @Override
            public void onFailure(String error) {
                showToastUI(PlayGameActivity.this, NETWORK_PROBLEM);
                runOnUiThread(() -> {
                    setAllButtonClickable(true);
                });
            }
        });
    }

    /**
     * This function should be called at the end of each round, to check whether the game is finished.
     */
    private void checkGameEnd(){
        recv(new onReceiveListener() {
            @Override
            public void onFailure(String error) {
                showToastUI(PlayGameActivity.this, NETWORK_PROBLEM);
                runOnUiThread(() -> {
                    setAllButtonClickable(true);
                });
            }

            @Override
            public void onSuccessful(Object object) {
                if (object instanceof String){
                    String result = (String) object;
                    if (result.equals(GAME_OVER)){
                        endGame();
                    }else {
                        newRound();
                    }
                }
            }
        });
    }

    /**
     * This function will receive the new round info from server.
     */
    private void newRound(){
        recv(new onReceiveListener() {
            @Override
            public void onFailure(String error) {
                showToastUI(PlayGameActivity.this, error);
                setAllButtonClickable(true);
            }

            @Override
            public void onSuccessful(Object object) {
                RoundInfo info = (RoundInfo) object;
                roundNum = info.getRoundNum();
                map = info.getMap();
                player = info.getPlayer();
                setPlayerID(player.getId());
                // clear all actions in the last round
                performedActions.clear();
                showToastUI(PlayGameActivity.this, String.format(Locale.US,"start round %d", roundNum));
                checkLose();
                runOnUiThread(() -> {
                    if (isLose){
                        // is user lose, hide all button
                        setAllButtonHidden();
                        if (!hasShowDialog){
                            AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
                            builder.setPositiveButton("Yes", (dialog1, which) -> {
                                showToastUI(PlayGameActivity.this, "you lose");
                            });
                            builder.setNegativeButton("No", (dialog2, which) -> {
                                onBackPressed();
                            });
                            builder.setTitle("You Lose");
                            builder.setMessage("Do you want to keep watching the game?");
                            AlertDialog dialog = builder.create();
                            dialog.setOnShowListener(dialog12 -> {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                        .setTextColor(
                                                getResources().getColor(R.color.colorPrimary)
                                        );
                            });
                            dialog.show();
                            hasShowDialog = true;
                        }
                        // do nothing, just waiting for attack result
                        receiveAttackResult();
                    }else {
                        // set all button clickable, let user input
                        setAllButtonClickable(true);
                    }
                    // as long as the user is in the room, we should update the info
                    // set the round number
                    tvRoundNum.setText(String.valueOf(roundNum));
                    // set the map image
                    imgMap.setImageResource(MAP_NAME_TO_RESOURCE_ID.get(map.getName()));
                    // update player info
                    showPlayerInfo();
                    // update territory list
                    showTerritories();
                });
            }
        });
    }

    /**
     * Update the territory list based on the latest map.
     */
    private void showTerritories(){
        List<Territory> territories = new ArrayList<>();
        for (Map.Entry<String, Territory> entry : map.getAtlas().entrySet()){
            territories.add(entry.getValue());
        }
        territoryAdapter.setTerritories(territories);
    }

    private void showPlayerInfo(){
        StringBuilder builder = new StringBuilder();
        builder.append("Player ").append(player.getName())
                .append("(id: ").append(player.getId()).append(")")
                .append("   ").append("Max Tech Level: ").append(player.getTechLevel())
                .append("\n");
        builder.append("Food resource: ").append(player.getFoodNum())
                .append("; Tech resource: ").append(player.getTechNum())
                .append("\n");
        tvPlayerInfo.setText(builder);
    }

    /**
     * Set the clickable property of all buttons in this page.
     * @param isClickable is clickable or not
     */
    private void setAllButtonClickable(boolean isClickable){
        btPerform.setClickable(isClickable);
    }

    private void setAllButtonHidden(){
        btPerform.setVisibility(View.GONE);
    }

    /**
     * This function will format and show the list of actions user already successfully performed.
     */
    private void showPerformedActions(){
        StringBuilder builder = new StringBuilder();
        builder.append("Actions performed:\n");
        if (performedActions.isEmpty()){
            builder.append("no action performed for now");
        }else {
            int index = 1;
            for (Action action : performedActions){
                builder.append(index).append(". ").append(action.toString()).append("\n");
                index ++;
            }
        }
        tvActionInfo.setText(builder);
    }

    private void endGame(){
        recv(new onReceiveListener() {
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "endGame" + error);
            }

            @Override
            public void onSuccessful(Object object) {
                String winnerInfo = (String) object;
                // dialog is a UI operation
                runOnUiThread(() -> {
                    // popup the game result and close the game after 3 seconds
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Result");
                    builder.setMessage(winnerInfo + "\n(this page will closed after 3 seconds)");
                    builder.show();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // kill this activity
                            finish();
                        }
                    }, 3000);
                });
            }
        });
    }

    private void checkLose(){
        List<Territory> territories = new ArrayList<>(map.getAtlas().values());
        for (Territory territory : territories){
            if (territory.getOwner() == getPlayerID()){
                isLose = false;
                return;
            }
        }
        isLose = true;
    }

    // probably want to extract this into constant
    private void goBack(){
        if (isLose){
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
            builder.setPositiveButton("Yes", (dialog1, which) -> {
                onBackPressed();
            });
            builder.setNegativeButton("No", (dialog2, which) -> {
            });

            builder.setTitle("Do you want to leave the game?");
            builder.setMessage("Since you already lose, once you leave, you can't join this game again.");
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog12 -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(
                                getResources().getColor(R.color.colorPrimary)
                        );
            });
            dialog.show();
        }else {
            // if not lose, can go out and come back as you want
            onBackPressed();
        }
    }
}
