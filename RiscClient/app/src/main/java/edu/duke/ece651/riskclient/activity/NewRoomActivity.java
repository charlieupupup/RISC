package edu.duke.ece651.riskclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import edu.duke.ece651.risk.shared.RoomInfo;
import edu.duke.ece651.risk.shared.map.MapDataBase;
import edu.duke.ece651.risk.shared.map.WorldMap;
import edu.duke.ece651.riskclient.R;
import edu.duke.ece651.riskclient.adapter.MapAdapter;
import edu.duke.ece651.riskclient.listener.onReceiveListener;
import edu.duke.ece651.riskclient.listener.onResultListener;

import static edu.duke.ece651.riskclient.Constant.MAP_NAME_TO_RESOURCE_ID;
import static edu.duke.ece651.riskclient.RiskApplication.recv;
import static edu.duke.ece651.riskclient.RiskApplication.setRoom;
import static edu.duke.ece651.riskclient.utils.HTTPUtils.sendNewRoomInfo;
import static edu.duke.ece651.riskclient.utils.UIUtils.showToastUI;

public class NewRoomActivity extends AppCompatActivity {

    private final static String TAG = NewRoomActivity.class.getSimpleName();

    private MapAdapter mapAdapter;
    private WorldMap selectedMap;

    /**
     * UI variable
     */
    private TextView tvMapName;
    private ImageView imgMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Add Room");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setUpUI();
        updateMaps();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpUI(){
        TextInputLayout tilRoomName = findViewById(R.id.til_room_name);
        TextInputEditText etRoomName = findViewById(R.id.et_room_name);

        etRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // remove the error message
                tilRoomName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button btCreate = findViewById(R.id.bt_create);
        btCreate.setOnClickListener(v -> {
            String roomName = Objects.requireNonNull(etRoomName.getText()).toString();
            if (roomName.isEmpty()){
                tilRoomName.setError("Room name can't be empty");
                return;
            }
            if (selectedMap == null){
                showToastUI(NewRoomActivity.this, "Please select a map.");
                return;
            }
            newRoom(roomName);
        });

        imgMap = findViewById(R.id.img_map);

        tvMapName = findViewById(R.id.tv_map_name);

        setUpRecyclerView();
    }

    private void setUpRecyclerView(){
        mapAdapter = new MapAdapter();
        mapAdapter.setListener(position -> {
            selectedMap = mapAdapter.getMap(position);
            tvMapName.setText(selectedMap.getName());
            imgMap.setImageResource(MAP_NAME_TO_RESOURCE_ID.get(selectedMap.getName()));
        });
        RecyclerView rvMapList = findViewById(R.id.rv_map_list);
        rvMapList.setHasFixedSize(true);
        rvMapList.setLayoutManager(new LinearLayoutManager(this));
        rvMapList.setAdapter(mapAdapter);
    }

    private void updateMaps(){
        recv(new onReceiveListener() {
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "updateMap: " + error);
            }

            @Override
            public void onSuccessful(Object object) {
                MapDataBase<String> mapDB = (MapDataBase<String>) object;
                Map<String, WorldMap<String>> allMaps = mapDB.getAllMaps();

                runOnUiThread(() -> {
                    mapAdapter.setMaps(new ArrayList<>(allMaps.values()));
                    // set default selected map
                    selectedMap = mapAdapter.getMap(0);
                    tvMapName.setText(selectedMap.getName());
                    imgMap.setImageResource(MAP_NAME_TO_RESOURCE_ID.get(selectedMap.getName()));
                });
            }
        });
    }

    private void newRoom(String roomName){
        sendNewRoomInfo(roomName, selectedMap.getName(), new onResultListener() {
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "newRoom: " + error);
            }

            @Override
            public void onSuccessful() {
                // set a temporary roomInfo object
                setRoom(new RoomInfo(1, roomName));
                showToastUI(NewRoomActivity.this, "Create new room successful.");
                Intent intent = new Intent(NewRoomActivity.this, WaitGameActivity.class);
                startActivity(intent);
                // kill current activity, user can't go back
                finish();
            }
        });
    }
}
