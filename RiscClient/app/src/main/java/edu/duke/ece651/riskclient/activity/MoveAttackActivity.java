package edu.duke.ece651.riskclient.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edu.duke.ece651.risk.shared.map.Unit;
import edu.duke.ece651.riskclient.R;
import edu.duke.ece651.riskclient.adapter.UnitAdapter;

import static edu.duke.ece651.riskclient.utils.UIUtils.showToastUI;

public class MoveAttackActivity extends AppCompatActivity {

    /**
     * UI variable
     */
    private TextView tvUnitsInfo;

    /**
     * Variable
     */
    private UnitAdapter srcUnitAdapter;
    private UnitAdapter destUnitAdapter;
    private List<Unit> srcUnits;
    private List<Unit> destUnits;
    private String actionType;
    private Map<Integer, Integer> units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_attack);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Move/Attack");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        actionType = "move";
        units = new TreeMap<>();
        setUpUI();
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
        Button btConfirm = findViewById(R.id.bt_confirm);
        Button btDecline = findViewById(R.id.bt_decline);

        btConfirm.setOnClickListener(v -> {
            // TODO: communicate with server
            Intent data = new Intent();
            setResult(RESULT_OK, data);
            finish();
        });
        btDecline.setOnClickListener(v -> {
            onBackPressed();
        });

        ImageView imgSendUnit = findViewById(R.id.img_add_unit);
        imgSendUnit.setOnClickListener(v -> {
            sendUnits();
        });

        tvUnitsInfo = findViewById(R.id.tv_units_info);
        tvUnitsInfo.setText("");

        setUpActionDropdown();
        setUpSrcTerritory();
        setUpDestTerritory();
    }

    /**
     * Popup a window to ask the user input units info
     */
    private void sendUnits(){
        LayoutInflater layoutInflater = getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.view_add_units, null);
        // level
        TextInputLayout tlLevel = view.findViewById(R.id.layout_level);
        AutoCompleteTextView dpLevel = tlLevel.findViewById(R.id.input);
        tlLevel.setHint("Unit Level");
        String[] items1 = new String[] {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<>(
                        MoveAttackActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        items1);
        dpLevel.setAdapter(adapter1);
        dpLevel.setText(items1[0], false);

        // number
        TextInputLayout tlNumber = view.findViewById(R.id.layout_number);
        AutoCompleteTextView dpNumber = tlNumber.findViewById(R.id.input);
        tlNumber.setHint("Unit Numbers");
        String[] items2 = new String[] {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(
                        MoveAttackActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        items2);
        dpNumber.setAdapter(adapter2);
        dpNumber.setText(items2[0], false);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Add Units");
        mBuilder.setView(view);
        mBuilder.setPositiveButton("Confirm", ((dialogInterface, i) -> {
            // TODO: validate data here
            Integer level = Integer.parseInt(dpLevel.getText().toString());
            Integer number = Integer.parseInt(dpNumber.getText().toString());
            if (units.containsKey(level)){
                int old = units.get(level);
                units.replace(level, old + number);
            }else {
                units.put(level, number);
            }
            refreshUnitsInfo();
        }));
        mBuilder.setNegativeButton("Cancel", ((dialogInterface, i) -> {
            showToastUI(MoveAttackActivity.this, "Cancel");
        }));
        mBuilder.create().show();
    }
    
    private void setUpActionDropdown(){
        TextInputLayout layout = findViewById(R.id.action_dropdown);
        layout.setHint("Action Type");

        String[] items = new String[] {"Move", "Attack"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        MoveAttackActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        items);

        AutoCompleteTextView dropdownAction =
                layout.findViewById(R.id.input);
        dropdownAction.setAdapter(adapter);
        dropdownAction.setText(items[0], false);
        dropdownAction.setOnItemClickListener((parent, v, position, id) -> {
            // TODO: update territory info
            showToastUI(MoveAttackActivity.this, "action: " + items[position]);
            actionType = (position == 0 ? "move" : "attack");
        });
    }

    private void setUpSrcTerritory(){
        // find the parent view of territory-unit-list
        View view = findViewById(R.id.src_territory);
        // setup drop down
        TextInputLayout layout = view.findViewById(R.id.dropdown_layout);
        layout.setHint("Src Territory");
        String[] items = new String[] {"Item 1", "Item 2", "Item 3", "Item 4"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        MoveAttackActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        items);

        AutoCompleteTextView dropdownSrcTerritory =
                view.findViewById(R.id.input);
        dropdownSrcTerritory.setAdapter(adapter);
        dropdownSrcTerritory.setText(items[0], false);
        dropdownSrcTerritory.setOnItemClickListener((parent, v, position, id) -> {
            // TODO: update units
            showToastUI(MoveAttackActivity.this, "src: " + items[position]);
        });

        // setup recycler view
        RecyclerView rvUnitList = view.findViewById(R.id.rv_unit_list);
        // TODO: replace with real data
        srcUnits = new ArrayList<>();
        for (int i = 0; i < 30; i++){
            srcUnits.add(new Unit("unit" + (i + 1)));
        }

        srcUnitAdapter = new UnitAdapter();
        srcUnitAdapter.setListener(position -> {
            Unit unit = srcUnits.get(position);
            showToastUI(MoveAttackActivity.this, "you click");
        });
        rvUnitList.setLayoutManager(new LinearLayoutManager(MoveAttackActivity.this));
        rvUnitList.setHasFixedSize(true);
        rvUnitList.setAdapter(srcUnitAdapter);
        srcUnitAdapter.setUnits(srcUnits);
    }

    private void setUpDestTerritory(){
        View view = findViewById(R.id.dest_territory);
        // setup drop down
        TextInputLayout layout = view.findViewById(R.id.dropdown_layout);
        layout.setHint("Dest Territory");
        String[] items = new String[] {"Item 1", "Item 2", "Item 3", "Item 4"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        MoveAttackActivity.this,
                        R.layout.dropdown_menu_popup_item,
                        items);

        AutoCompleteTextView dropdownDestTerritory =
                view.findViewById(R.id.input);
        dropdownDestTerritory.setAdapter(adapter);
        dropdownDestTerritory.setText(items[0], false);
        dropdownDestTerritory.setOnItemClickListener((parent, v, position, id) -> {
            // TODO: update units
            showToastUI(MoveAttackActivity.this, "dest: " + items[position]);
        });

        // setup recycler view
        RecyclerView rvUnitList = view.findViewById(R.id.rv_unit_list);
        // TODO: replace with real data
        destUnits = new ArrayList<>();
        for (int i = 0; i < 30; i++){
            destUnits.add(new Unit("unit" + (i + 1)));
        }

        destUnitAdapter = new UnitAdapter();
        destUnitAdapter.setListener(position -> {
            Unit unit = destUnits.get(position);
            showToastUI(MoveAttackActivity.this, "you click");
        });
        rvUnitList.setLayoutManager(new LinearLayoutManager(MoveAttackActivity.this));
        rvUnitList.setHasFixedSize(true);
        rvUnitList.setAdapter(destUnitAdapter);
        destUnitAdapter.setUnits(destUnits);
    }

    private void refreshUnitsInfo(){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : units.entrySet()){
            builder.append(entry.getValue()).append(" units of level ").append(entry.getKey()).append("\n");
        }
        tvUnitsInfo.setText(builder.toString());
    }
}