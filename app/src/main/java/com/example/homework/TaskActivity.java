package com.example.homework;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homework.data.Task;
import com.example.homework.listener.OnItemClickListener;
import com.example.homework.adapter.TasksRecyclerViewAdapter;
import com.example.homework.data.LongTermTask;
import com.example.homework.data.PeriodicTask;
import com.example.homework.data.TaskKind;
import com.example.homework.data.TemporaryTask;
import com.example.homework.listener.OnItemTouchListener;
import com.example.homework.utils.MyItemTouchHelper;
import com.example.homework.utils.PermissionUtil;
import com.example.homework.utils.TimeUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.homework.MainActivity.CHILD_UPDATE_MODE;
import static com.example.homework.MainActivity.CREATE_MODE;
import static com.example.homework.MainActivity.UPDATE_MODE;
import static com.example.homework.MainActivity.CHILD_CREATE_MODE;
import static com.example.homework.data.TaskKind.TEMPORARYTASK;
import static com.example.homework.data.TaskKind.PERIODICTASK;
import static com.example.homework.data.TaskKind.LONGTERMTASK;
import static com.example.homework.utils.DataTypeUtil.isInteger;
import static com.example.homework.utils.MyActivityManager.getRunningActivity;
import static com.example.homework.utils.SerializeAndDeserialize.deSerializeforLongTermTask;
import static com.example.homework.utils.SerializeAndDeserialize.deSerializeforTask;
import static com.example.homework.utils.SerializeAndDeserialize.deSerializeforTaskList;
import static com.example.homework.utils.SerializeAndDeserialize.serializeforLongtermTask;
import static com.example.homework.utils.SerializeAndDeserialize.serializeforTaskList;

@SuppressWarnings("unchecked")
public class TaskActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 2;

    private List<TemporaryTask> temporaryTaskList;
    private List<PeriodicTask> periodicTaskList;
    private List<LongTermTask> longTermTaskList;

    private int mode;
    private String taskListName;
    private TaskKind taskKind;
    private boolean sortByCreateTimeFlag = false;
    private boolean sortByDeadlineFlag = false;
    private boolean sortByStartDateFlag = false;
    private boolean sortByTaskNameFlag = false;
    private boolean sortByChildCountFlag = false;
    private int position;
    private int parentPosition;

    TextView taskMenuButton;
    TextView backToTaskFactoryButton;
    TextView taskListTitleTextView;
    TextView insertTextView;
    RecyclerView taskListRecyclerView;
    TasksRecyclerViewAdapter tasksRecyclerViewAdapter;
    LocalBroadcastManager localBroadcastManager;
    LinearLayoutManager taskListRecyclerViewLayoutManager;
    LocalReceiver localReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        initEvent();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tasksRecyclerViewAdapter.notifyDataSetChanged();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcast.LOCAL_BROADCAST");
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    private void initEvent(){
        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", -1);
        taskKind = TaskKind.values()[intent.getIntExtra("task_factory_kind", -1)] ;
        if(mode == CREATE_MODE) {
            taskListName = intent.getStringExtra("task_factory_name");
            switch (taskKind) {
                case TEMPORARYTASK:
                    temporaryTaskList = new ArrayList<>();
                    break;
                case PERIODICTASK:
                    periodicTaskList = new ArrayList<>();
                    break;
                case LONGTERMTASK:
                    longTermTaskList = new ArrayList<>();
                    break;
                default:
                    break;
            }
        } else if (mode == UPDATE_MODE) {
            taskListName = intent.getStringExtra("task_factory_name");
            position = intent.getIntExtra("position", -1);
            try {
                switch (taskKind) {
                    case TEMPORARYTASK:
                        temporaryTaskList = (List<TemporaryTask>)deSerializeforTaskList(intent.getStringExtra("task_list"), taskKind);
                        break;
                    case PERIODICTASK:
                        periodicTaskList = (List<PeriodicTask>)deSerializeforTaskList(intent.getStringExtra("task_list"), taskKind);
                        break;
                    case LONGTERMTASK:
                        longTermTaskList = (List<LongTermTask>)deSerializeforTaskList(intent.getStringExtra("task_list"), taskKind);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (mode == CHILD_CREATE_MODE) {
            parentPosition = intent.getIntExtra("parent_position", -1);
            taskListName = intent.getStringExtra("parent_name");
            longTermTaskList = new ArrayList<>();
            LongTermTask longTermTask = null;
            try {
                longTermTask = deSerializeforLongTermTask(intent.getStringExtra("long_term_task"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            longTermTaskList.add(longTermTask);
        } else if (mode == CHILD_UPDATE_MODE) {
            parentPosition = intent.getIntExtra("parent_position", -1);
            taskListName = intent.getStringExtra("parent_name");
            try {
                longTermTaskList = (List<LongTermTask>) deSerializeforTaskList(intent.getStringExtra("long_term_task_list"), LONGTERMTASK);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportTaskFactory() {
        final EditText editText = new EditText(TaskActivity.this);
        TextView textView = new TextView(TaskActivity.this);
        textView.setText("保存的文件在SD卡根目录下");
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        editText.setHint("请输入导出的文件名");
        LinearLayout linearLayout = new LinearLayout(TaskActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                .setCancelable(false)
                .setTitle("导出为文件")
                .setView(linearLayout)
                .setPositiveButton("导出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fileName = editText.getText().toString().trim().toLowerCase();
                        if(fileName.equals("")) {
                            Toast.makeText(TaskActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else {
//                            File file = new File("/data/data/com.example.homework", "task");
//                            if (!file.exists())
//                                file.mkdirs();
//                            File task = new File(file, fileName + ".txt");
//                            if (task.exists())
//                                task.delete();
//                            try {
//                                task.createNewFile();
//                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(task));
//                                objectOutputStream.writeObject(serializeforTaskFactoryList(taskFactoryList));
//                                objectOutputStream.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                            PermissionUtil.verifyStoragePermissions(TaskActivity.this);
                            try {
                                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                                BufferedOutputStream bufferedOutputStream;
                                if(taskKind == TEMPORARYTASK) {
                                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(sdPath + "/" + fileName + ".tt",true));
                                    bufferedOutputStream.write(serializeforTaskList(temporaryTaskList).getBytes());
                                } else if (taskKind == PERIODICTASK) {
                                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(sdPath + "/" + fileName + ".pt",true));
                                    bufferedOutputStream.write(serializeforTaskList(periodicTaskList).getBytes());
                                } else {
                                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(sdPath + "/" + fileName + ".lt",true));
                                    bufferedOutputStream.write(serializeforTaskList(longTermTaskList).getBytes());
                                }
                                bufferedOutputStream.close();
                                Toast.makeText(TaskActivity.this, "导出成功", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(TaskActivity.this, "导出失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消", null);
        builder.show();
    }

    private void importTaskFactory() {
        final EditText editText = new EditText(TaskActivity.this);
        TextView textView = new TextView(TaskActivity.this);
        textView.setText("从SD卡根目录下导入文件\n后缀:临时任务是tt,周期任务是pt,长期任务是lt");
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        editText.setHint("请输入导入的文件名(带完整后缀)");
        LinearLayout linearLayout = new LinearLayout(TaskActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                .setCancelable(false)
                .setTitle("导入文件")
                .setView(linearLayout)
                .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fileName = editText.getText().toString().trim().toLowerCase();
                        String fileKind = fileName.substring(fileName.lastIndexOf(".") + 1);
                        int kind = -1;
                        switch (fileKind) {
                            case "tt":
                                kind = 0;
                                break;
                            case "pt":
                                kind = 1;
                                break;
                            case "lt":
                                kind = 2;
                                break;
                        }
                        if(fileName.equals("")) {
                            Toast.makeText(TaskActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else if (!fileKind.equals("tt") && !fileKind.equals("pt") && !fileKind.equals("lt")) {
                            Toast.makeText(TaskActivity.this, "文件类型有误", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else if (kind != taskKind.ordinal()) {
                            Toast.makeText(TaskActivity.this, "任务类型与当前任务列表类型不一致", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else {
                            PermissionUtil.verifyStoragePermissions(TaskActivity.this);
                            try {
                                File file = new File(Environment.getExternalStorageDirectory(), fileName);
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                                String readline;
                                StringBuilder stringBuilder = new StringBuilder();
                                while ((readline = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(readline);
                                }
                                bufferedReader.close();
                                switch (fileKind) {
                                    case "tt":
                                        temporaryTaskList.addAll((List<TemporaryTask>) deSerializeforTaskList(stringBuilder.toString(), TEMPORARYTASK));
                                        break;
                                    case "pt":
                                        periodicTaskList.addAll((List<PeriodicTask>) deSerializeforTaskList(stringBuilder.toString(), PERIODICTASK));
                                        break;
                                    case "lt":
                                        longTermTaskList.addAll((List<LongTermTask>) deSerializeforTaskList(stringBuilder.toString(), LONGTERMTASK));
                                        break;
                                }
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                                Toast.makeText(TaskActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(TaskActivity.this, "导入失败  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消", null);
        builder.show();
    }

    private void initView(){
        taskMenuButton = findViewById(R.id.task_menu_button);
        taskMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(TaskActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.task_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.add_task_button:
                                createTask();
                                return true;
                            case R.id.insert_task_button:
                                insertATask(taskListRecyclerView.getChildCount());
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.find_task_button:
                                findATask();
                                return true;
                            case R.id.sort_task_button:
                                sortTask();
                                return true;
                            case R.id.import_task_button:
                                importTaskFactory();
                                return true;
                            case R.id.export_task_button:
                                exportTaskFactory();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        taskListTitleTextView = findViewById(R.id.task_list_title_text_view);
        taskListTitleTextView.setText(taskListName);

        backToTaskFactoryButton = findViewById(R.id.back_to_task_factory_button);
        backToTaskFactoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                if(mode == CREATE_MODE || mode == UPDATE_MODE) {
                    intent.putExtra("task_list_name", taskListName);
                    try{
                        if(taskKind == TEMPORARYTASK) {
                            intent.putExtra("task_list", serializeforTaskList(temporaryTaskList));
                        }
                        else if(taskKind == PERIODICTASK) {
                            intent.putExtra("task_list", serializeforTaskList(periodicTaskList));
                        }
                        else if(taskKind == LONGTERMTASK) {
                            intent.putExtra("task_list", serializeforTaskList(longTermTaskList));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("task_kind", taskKind.ordinal());
                    intent.putExtra("position", position);
                    intent.putExtra("operate_mode", mode);
                    setResult(RESULT_FIRST_USER, intent);
                } else if (mode == CHILD_CREATE_MODE || mode == CHILD_UPDATE_MODE) {
                    try {
                        intent.putExtra("child_task", serializeforTaskList(longTermTaskList));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("parent_task_position", parentPosition);
                }
                setResult(RESULT_FIRST_USER, intent);
                TaskActivity.this.finish();
            }
        });

        insertTextView = findViewById(R.id.insert_text_view);
        insertTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
                builder.setTitle("粘贴任务")
                        .setPositiveButton("粘贴", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                insertATask(taskListRecyclerView.getChildCount());
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });
        taskListRecyclerView = findViewById(R.id.task_list_recycler_view);
        //设置布局管理器
        taskListRecyclerViewLayoutManager = new LinearLayoutManager(TaskActivity.this);
        taskListRecyclerView.setLayoutManager(taskListRecyclerViewLayoutManager);
        if(taskKind == TEMPORARYTASK) {
            tasksRecyclerViewAdapter = new TasksRecyclerViewAdapter(temporaryTaskList, taskKind, TaskActivity.this);
        } else if (taskKind == PERIODICTASK) {
            tasksRecyclerViewAdapter = new TasksRecyclerViewAdapter(periodicTaskList, taskKind, TaskActivity.this);
        } else if (taskKind == LONGTERMTASK) {
            tasksRecyclerViewAdapter = new TasksRecyclerViewAdapter(longTermTaskList, taskKind, TaskActivity.this);
        }
        //设置Adapter
        taskListRecyclerView.setAdapter(tasksRecyclerViewAdapter);
        tasksRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                updateTask(position);
                tasksRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        MyItemTouchHelper touchHelper = new MyItemTouchHelper(new OnItemTouchListener() {
            //拖动排序
            @Override
            public boolean onMove(int fromPosition, int toPosition) {
                if (fromPosition < toPosition) {
                    //从上往下拖动，每滑动一个item，都将list中的item向下交换，向上滑同理。
                    for (int i = fromPosition; i < toPosition; i++) {
                        if(taskKind == TEMPORARYTASK) Collections.swap(temporaryTaskList, i, i + 1);
                        else if (taskKind == PERIODICTASK) Collections.swap(periodicTaskList, i, i + 1);
                        else if (taskKind == LONGTERMTASK) Collections.swap(longTermTaskList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        if(taskKind == TEMPORARYTASK) Collections.swap(temporaryTaskList, i, i - 1);
                        else if (taskKind == PERIODICTASK) Collections.swap(periodicTaskList, i, i - 1);
                        else if (taskKind == LONGTERMTASK) Collections.swap(longTermTaskList, i, i - 1);
                    }
                }
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //notifyItemMoved只是告诉RecyclerView它的Item换了位置，也就是说如果没有它那么你只能拖动
                //某一项而不能让它与其他项互换位置但是notifyItemMoved仅仅是互换了界面上的项，你必须还要
                //通知RecyclerView它的内容也改变了，这就要用notifyItemRangeChanged
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                tasksRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition);
                tasksRecyclerViewAdapter.notifyItemRangeChanged(Math.min(fromPosition, toPosition),
                        Math.abs(fromPosition - toPosition) +1);
                return true;
            }
            //滑动删除
            @Override
            public void onSwiped(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
                builder.setCancelable(false)
                        .setTitle("确认删除该任务吗")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(taskKind == TEMPORARYTASK) temporaryTaskList.remove(position);
                                else if (taskKind == PERIODICTASK) periodicTaskList.remove(position);
                                else if (taskKind == LONGTERMTASK) longTermTaskList.remove(position);
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .show();
            }
        });
        touchHelper.setSort(true);//打开拖动排序
        touchHelper.setDelete(true);//打开滑动删除
        new ItemTouchHelper(touchHelper).attachToRecyclerView(taskListRecyclerView);
    }

    private void createTask(){
        if(taskKind == TEMPORARYTASK) {
            View view = LayoutInflater.from(TaskActivity.this).inflate(R.layout.temporary_task_dialog_layout, null);
            final TextView temporaryTaskTitleTextView = view.findViewById(R.id.temporary_task_title_text_view);
            temporaryTaskTitleTextView.setText("设 置 临 时 任 务");
            final EditText temporaryTaskNameEditText = view.findViewById(R.id.temporary_task_name_edit_text);
            final TextView temporaryTaskDeadlineEditText = view.findViewById(R.id.temporary_task_deadline_edit_text);
            final Calendar temporaryTaskDeadlineCalendar = Calendar.getInstance();
            temporaryTaskDeadlineEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePicker datePicker = new DatePicker(TaskActivity.this);
                    datePicker.init(temporaryTaskDeadlineCalendar.get(Calendar.YEAR), temporaryTaskDeadlineCalendar.get(Calendar.MONTH), temporaryTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            temporaryTaskDeadlineCalendar.set(year, monthOfYear, dayOfMonth);
                        }
                    });
                    new AlertDialog.Builder(TaskActivity.this)
                            .setView(datePicker)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar nowCalendar = Calendar.getInstance();
                                    if(temporaryTaskDeadlineCalendar.before(nowCalendar)){
                                        Toast.makeText(TaskActivity.this, "截止日期无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        temporaryTaskDeadlineEditText.setText(temporaryTaskDeadlineCalendar.get(Calendar.YEAR) + "年" + (temporaryTaskDeadlineCalendar.get(Calendar.MONTH) + 1) + "月" + temporaryTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH) + "日");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            final EditText temporaryTaskDetailEditText = view.findViewById(R.id.temporary_task_detail_edit_text);
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String temporaryTaskName = temporaryTaskNameEditText.getText().toString().trim();
                            String temporaryTaskDeadline = temporaryTaskDeadlineEditText.getText().toString();
                            String temporaryTaskDetail = temporaryTaskDetailEditText.getText().toString();
                            if(temporaryTaskName.equals("") || temporaryTaskDeadline.equals("")){
                                Toast.makeText(TaskActivity.this, "任务名或截止日期不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                                Calendar temporaryTaskCreateTimeCalendar = TimeUtil.getMinuteCreateTimeCalendar();
                                TemporaryTask temporaryTask = new TemporaryTask(temporaryTaskName, temporaryTaskDetail, temporaryTaskDeadlineCalendar, temporaryTaskCreateTimeCalendar);
                                temporaryTaskList.add(temporaryTask);
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        }
                    }).setNegativeButton("取消", null);
            builder.show();
        } else if (taskKind == PERIODICTASK) {
            View view = LayoutInflater.from(TaskActivity.this).inflate(R.layout.periodic_task_dialog_layout, null);
            final EditText periodicTaskNameEditText = view.findViewById(R.id.periodic_task_name_edit_text);
            final EditText periodicTaskRepetitionTimesEditText = view.findViewById(R.id.periodic_task_repetition_times_edit_text);
            final EditText periodicTaskRepetitionPeriodEditText = view.findViewById(R.id.periodic_task_repetition_period_edit_text);
            final TextView periodicTaskStartDateEditText = view.findViewById(R.id.periodic_task_start_date_edit_text);
            final Calendar periodicTaskStartDateCalendar = Calendar.getInstance();
            periodicTaskStartDateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePicker datePicker = new DatePicker(TaskActivity.this);
                    datePicker.init(periodicTaskStartDateCalendar.get(Calendar.YEAR), periodicTaskStartDateCalendar.get(Calendar.MONTH), periodicTaskStartDateCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            periodicTaskStartDateCalendar.set(year, monthOfYear, dayOfMonth);
                        }
                    });
                    new AlertDialog.Builder(TaskActivity.this)
                            .setView(datePicker)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar nowCalendar = Calendar.getInstance();
                                    if(periodicTaskStartDateCalendar.before(nowCalendar)){
                                        Toast.makeText(TaskActivity.this, "执行日期无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        periodicTaskStartDateEditText.setText(periodicTaskStartDateCalendar.get(Calendar.YEAR) + "年" + (periodicTaskStartDateCalendar.get(Calendar.MONTH) + 1) + "月" + periodicTaskStartDateCalendar.get(Calendar.DAY_OF_MONTH) + "日");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            final EditText periodicTaskDetailEditText = view.findViewById(R.id.periodic_task_detail_edit_text);
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String periodicTaskName = periodicTaskNameEditText.getText().toString().trim();
                            String periodicTaskStartDate = periodicTaskStartDateEditText.getText().toString();
                            String periodicTaskDetail = periodicTaskDetailEditText.getText().toString();
                            String periodicTaskRepetitionTimes = periodicTaskRepetitionTimesEditText.getText().toString().trim();
                            String periodicTaskRepetitionPeriod = periodicTaskRepetitionPeriodEditText.getText().toString().trim();
                            if(periodicTaskName.equals("") || periodicTaskStartDate.equals("")) {
                                Toast.makeText(TaskActivity.this, "任务名或执行日期不能为空", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(!isInteger(periodicTaskRepetitionTimes) || !isInteger(periodicTaskRepetitionPeriod)) {
                                Toast.makeText(TaskActivity.this, "重复次数或重复周期必须为整数", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Calendar periodicTaskCreateTimeCalendar = TimeUtil.getMinuteCreateTimeCalendar();
                            PeriodicTask periodicTask = new PeriodicTask(periodicTaskName, periodicTaskDetail,
                                    periodicTaskStartDateCalendar, periodicTaskRepetitionTimes, periodicTaskRepetitionPeriod, periodicTaskCreateTimeCalendar);
                            periodicTaskList.add(periodicTask);
                            tasksRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton("取消", null);
            builder.show();
        } else if (taskKind == LONGTERMTASK) {
            View view = LayoutInflater.from(TaskActivity.this).inflate(R.layout.temporary_task_dialog_layout, null);
            final TextView longTermTaskTitleTextView = view.findViewById(R.id.temporary_task_title_text_view);
            longTermTaskTitleTextView.setText("设 置 长 期 任 务");
            final EditText longTermTaskNameEditText = view.findViewById(R.id.temporary_task_name_edit_text);
            final TextView longTermTaskDeadlineEditText = view.findViewById(R.id.temporary_task_deadline_edit_text);
            final Calendar longTermTaskDeadlineCalendar = Calendar.getInstance();
            longTermTaskDeadlineEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePicker datePicker = new DatePicker(TaskActivity.this);
                    datePicker.init(longTermTaskDeadlineCalendar.get(Calendar.YEAR), longTermTaskDeadlineCalendar.get(Calendar.MONTH), longTermTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            longTermTaskDeadlineCalendar.set(year, monthOfYear, dayOfMonth);
                        }
                    });
                    new AlertDialog.Builder(TaskActivity.this)
                            .setView(datePicker)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar nowCalendar = Calendar.getInstance();
                                    if(longTermTaskDeadlineCalendar.before(nowCalendar)){
                                        Toast.makeText(TaskActivity.this, "截止日期无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        longTermTaskDeadlineEditText.setText(longTermTaskDeadlineCalendar.get(Calendar.YEAR) + "年" + (longTermTaskDeadlineCalendar.get(Calendar.MONTH) + 1) + "月" + longTermTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH) + "日");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            final EditText longTermTaskDetailEditText = view.findViewById(R.id.temporary_task_detail_edit_text);
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String longTermTaskName = longTermTaskNameEditText.getText().toString().trim();
                            String longTermTaskDeadline = longTermTaskDeadlineEditText.getText().toString();
                            String longTermTaskDetail = longTermTaskDetailEditText.getText().toString();
                            if(longTermTaskName.equals("") || longTermTaskDeadline.equals("")){
                                Toast.makeText(TaskActivity.this, "任务名或截止日期不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                                LongTermTask longTermTask = new LongTermTask(longTermTaskName, longTermTaskDetail, longTermTaskDeadlineCalendar, TimeUtil.getMinuteCreateTimeCalendar());
                                longTermTaskList.add(longTermTask);
                                tasksRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        }
                    }).setNegativeButton("取消", null);
            builder.show();
        }
    }

    private void updateTask(final int position){
        if(taskKind == TEMPORARYTASK || taskKind == LONGTERMTASK) {
            View view = LayoutInflater.from(TaskActivity.this).inflate(R.layout.temporary_task_dialog_layout, null);
            final TextView longTermTaskTitleTextView = view.findViewById(R.id.temporary_task_title_text_view);
            if(taskKind == LONGTERMTASK) longTermTaskTitleTextView.setText("设 置 长 期 任 务");
            else if(taskKind == TEMPORARYTASK) longTermTaskTitleTextView.setText("设 置 临 时 任 务");
            final EditText temporaryTaskNameEditText = view.findViewById(R.id.temporary_task_name_edit_text);
            final EditText temporaryTaskDetailEditText = view.findViewById(R.id.temporary_task_detail_edit_text);
            final TextView temporaryTaskDeadlineEditText = view.findViewById(R.id.temporary_task_deadline_edit_text);
            if(taskKind == TEMPORARYTASK) {
                temporaryTaskNameEditText.setText(temporaryTaskList.get(position).getName());
                temporaryTaskDeadlineEditText.setText(temporaryTaskList.get(position).getDeadline());
                temporaryTaskDetailEditText.setText(temporaryTaskList.get(position).getDetail());
            } else if (taskKind == LONGTERMTASK) {
                temporaryTaskNameEditText.setText(longTermTaskList.get(position).getName());
                temporaryTaskDeadlineEditText.setText(longTermTaskList.get(position).getDeadline());
                temporaryTaskDetailEditText.setText(longTermTaskList.get(position).getDetail());
            }
            temporaryTaskDetailEditText.setSelection(temporaryTaskDetailEditText.length());
            temporaryTaskNameEditText.setSelection(temporaryTaskNameEditText.length());
            final Calendar temporaryTaskDeadlineCalendar = Calendar.getInstance();
            temporaryTaskDeadlineEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePicker datePicker = new DatePicker(TaskActivity.this);
                    datePicker.init(temporaryTaskDeadlineCalendar.get(Calendar.YEAR), temporaryTaskDeadlineCalendar.get(Calendar.MONTH), temporaryTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            temporaryTaskDeadlineCalendar.set(year, monthOfYear, dayOfMonth);
                        }
                    });
                    new AlertDialog.Builder(TaskActivity.this)
                            .setView(datePicker)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar nowCalendar = Calendar.getInstance();
                                    if(temporaryTaskDeadlineCalendar.before(nowCalendar)){
                                        Toast.makeText(TaskActivity.this, "截止日期无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        temporaryTaskDeadlineEditText.setText(temporaryTaskDeadlineCalendar.get(Calendar.YEAR) + "年" + (temporaryTaskDeadlineCalendar.get(Calendar.MONTH) + 1) + "月" + temporaryTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH) + "日");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String temporaryTaskName = temporaryTaskNameEditText.getText().toString().trim();
                            String temporaryTaskDeadline = temporaryTaskDeadlineEditText.getText().toString().trim();
                            String temporaryTaskDetail = temporaryTaskDetailEditText.getText().toString();
                            if(temporaryTaskName.equals("") || temporaryTaskDeadline.equals("")){
                                Toast.makeText(TaskActivity.this, "任务名或截止日期不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                                if(taskKind == TEMPORARYTASK) {
                                    temporaryTaskList.get(position).setName(temporaryTaskName);
                                    temporaryTaskList.get(position).setDetail(temporaryTaskDetail);
                                    temporaryTaskList.get(position).setDeadlineCalendar(temporaryTaskDeadlineCalendar);
                                } else if (taskKind == LONGTERMTASK) {
                                    longTermTaskList.get(position).setName(temporaryTaskName);
                                    longTermTaskList.get(position).setDetail(temporaryTaskDetail);
                                    longTermTaskList.get(position).setDeadlineCalendar(temporaryTaskDeadlineCalendar);
                                }
                                tasksRecyclerViewAdapter.notifyItemChanged(position);
                            }
                        }
                    }).setNegativeButton("取消", null);
            builder.show();
        } else if(taskKind == PERIODICTASK) {
            View view = LayoutInflater.from(TaskActivity.this).inflate(R.layout.periodic_task_dialog_layout, null);
            final EditText periodicTaskNameEditText = view.findViewById(R.id.periodic_task_name_edit_text);
            final EditText periodicTaskRepetitionTimesEditText = view.findViewById(R.id.periodic_task_repetition_times_edit_text);
            final EditText periodicTaskRepetitionPeriodEditText = view.findViewById(R.id.periodic_task_repetition_period_edit_text);
            periodicTaskNameEditText.setText(periodicTaskList.get(position).getName());
            periodicTaskNameEditText.setSelection(periodicTaskNameEditText.length());
            periodicTaskRepetitionTimesEditText.setText(periodicTaskList.get(position).getRepetitionTimes());
            periodicTaskRepetitionTimesEditText.setSelection(periodicTaskRepetitionTimesEditText.length());
            periodicTaskRepetitionPeriodEditText.setText(periodicTaskList.get(position).getRepetitionPeriod());
            periodicTaskRepetitionPeriodEditText.setSelection(periodicTaskRepetitionPeriodEditText.length());
            final TextView periodicTaskStartDateEditText = view.findViewById(R.id.periodic_task_start_date_edit_text);
            final Calendar periodicTaskStartDateCalendar = Calendar.getInstance();
            periodicTaskStartDateEditText.setText(periodicTaskList.get(position).getStartDate());
            periodicTaskStartDateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePicker datePicker = new DatePicker(TaskActivity.this);
                    datePicker.init(periodicTaskStartDateCalendar.get(Calendar.YEAR), periodicTaskStartDateCalendar.get(Calendar.MONTH), periodicTaskStartDateCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            periodicTaskStartDateCalendar.set(year, monthOfYear, dayOfMonth);
                        }
                    });
                    new AlertDialog.Builder(TaskActivity.this)
                            .setView(datePicker)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar nowCalendar = Calendar.getInstance();
                                    if(periodicTaskStartDateCalendar.before(nowCalendar)){
                                        Toast.makeText(TaskActivity.this, "执行日期无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        periodicTaskStartDateEditText.setText(periodicTaskStartDateCalendar.get(Calendar.YEAR) + "年" + (periodicTaskStartDateCalendar.get(Calendar.MONTH) + 1) + "月" + periodicTaskStartDateCalendar.get(Calendar.DAY_OF_MONTH) + "日");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            final EditText periodicTaskDetailEditText = view.findViewById(R.id.periodic_task_detail_edit_text);
            periodicTaskDetailEditText.setText(periodicTaskList.get(position).getDetail());
            periodicTaskDetailEditText.setSelection(periodicTaskDetailEditText.length());
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String periodicTaskName = periodicTaskNameEditText.getText().toString().trim();
                            String periodicTaskStartDate = periodicTaskStartDateEditText.getText().toString().trim();
                            String periodicTaskDetail = periodicTaskDetailEditText.getText().toString();
                            String periodicTaskRepetitionTimes = periodicTaskRepetitionTimesEditText.getText().toString().trim();
                            String periodicTaskRepetitionPeriod = periodicTaskRepetitionPeriodEditText.getText().toString().trim();
                            if(periodicTaskName.equals("") || periodicTaskStartDate.equals("")){
                                Toast.makeText(TaskActivity.this, "任务名或执行日期不能为空", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(!isInteger(periodicTaskRepetitionTimes) || !isInteger(periodicTaskRepetitionPeriod)) {
                                Toast.makeText(TaskActivity.this, "重复次数或重复周期必须为整数", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else {
                                periodicTaskList.get(position).setName(periodicTaskName);
                                periodicTaskList.get(position).setDetail(periodicTaskDetail);
                                periodicTaskList.get(position).setRepetitionTimes(periodicTaskRepetitionTimes);
                                periodicTaskList.get(position).setRepetitionTimes(periodicTaskRepetitionPeriod);
                                periodicTaskList.get(position).setStartDateCalendar(periodicTaskStartDateCalendar);
                                tasksRecyclerViewAdapter.notifyItemChanged(position);
                            }
                        }
                    }).setNegativeButton("取消", null);
            builder.show();
        }
    }

    private void findATask() {
        final EditText findTaskEditText = new EditText(TaskActivity.this);
        findTaskEditText.setHint("请输入任务名");
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                .setView(findTaskEditText)
                .setTitle("查找任务")
                .setPositiveButton("查找", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String taskName = findTaskEditText.getText().toString().trim();
                        showTask(taskName);
                    }
                })
                .setNegativeButton("取消", null);
        builder.show();
    }

    private void showTask(String taskName) {
        int index = -1;
        boolean isFind = false;
        List<? extends Task> taskList;
        switch (taskKind) {
            case TEMPORARYTASK:
                taskList = temporaryTaskList;
                break;
            case PERIODICTASK:
                taskList = periodicTaskList;
                break;
            case LONGTERMTASK:
                taskList = longTermTaskList;
                break;
            default:
                taskList = new ArrayList<>();
                break;
        }
        for(Task task : taskList) {
            if(task.getName().equals(taskName)) {
                index++;
                isFind = true;
                break;
            }
            index++;
        }
        if(isFind) {
            switch (taskKind) {
                case TEMPORARYTASK:
                    Collections.swap(temporaryTaskList, 0, index);
                    break;
                case PERIODICTASK:
                    Collections.swap(periodicTaskList, 0, index);
                    break;
                case LONGTERMTASK:
                    Collections.swap(longTermTaskList, 0, index);
                    break;
                default:
                    break;
            }
            tasksRecyclerViewAdapter.notifyDataSetChanged();

            ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#00000000"), Color.parseColor("#999999"));
            colorAnim.setDuration(450).setRepeatCount(2);
            colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator colorAnimator) {
                    taskListRecyclerView.getChildAt(0).setBackgroundColor((int) colorAnimator.getAnimatedValue());
                }
            });
            colorAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    taskListRecyclerView.getChildAt(0).setBackgroundColor(Color.parseColor("#00000000"));
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            colorAnim.setInterpolator(new LinearInterpolator());
            colorAnim.start();
        } else Toast.makeText(TaskActivity.this, "未找到该任务", Toast.LENGTH_SHORT).show();

//        int firstItem = taskListRecyclerViewLayoutManager.findFirstVisibleItemPosition();
//        int lastItem = taskListRecyclerViewLayoutManager.findLastVisibleItemPosition();
//        if (position <= firstItem) {
//            taskListRecyclerView.scrollToPosition(position);
//        } else if (position <= lastItem) {
//            int top = taskListRecyclerView.getChildAt(position - firstItem).getTop();
//            taskListRecyclerView.scrollBy(0, top);
//        } else {
//            taskListRecyclerView.scrollToPosition(position);
//        }

    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int which = intent.getIntExtra("do_which", -1);
            int position = intent.getIntExtra("position", -1);
            doSomethingAccordingToTaskMenu(which, position);
        }
    }

    public void doSomethingAccordingToTaskMenu(int which, int position){
        if(taskKind == TEMPORARYTASK){
            switch (which) {
                case TasksRecyclerViewAdapter.MENU_DELETE_THIS_TASK:
                    temporaryTaskList.remove(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case TasksRecyclerViewAdapter.MENU_CHANGE_TASK_STATE:
                    if(temporaryTaskList.get(position).isFinished()) temporaryTaskList.get(position).setFinished(false);
                    else temporaryTaskList.get(position).setFinished(true);
                    tasksRecyclerViewAdapter.notifyItemChanged(position);
                    break;
                case TasksRecyclerViewAdapter.MENU_CUT_THIS_TASK:
                    temporaryTaskList.remove(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case TasksRecyclerViewAdapter.MENU_INSERT_A_TASK:
                    insertATask(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        } else if (taskKind == PERIODICTASK) {
            switch (which) {
                case TasksRecyclerViewAdapter.MENU_DELETE_THIS_TASK:
                    periodicTaskList.remove(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case TasksRecyclerViewAdapter.MENU_CHANGE_TASK_STATE:
                    if(periodicTaskList.get(position).isFinished()) periodicTaskList.get(position).setFinished(false);
                    else periodicTaskList.get(position).setFinished(true);
                    tasksRecyclerViewAdapter.notifyItemChanged(position);
                    break;
                case TasksRecyclerViewAdapter.MENU_CUT_THIS_TASK:
                    periodicTaskList.remove(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case TasksRecyclerViewAdapter.MENU_INSERT_A_TASK:
                    insertATask(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        } else if (taskKind == LONGTERMTASK) {
            switch (which) {
                case TasksRecyclerViewAdapter.MENU_CREATE_CHILD_TASK:
                    createChildTaskInLongTermTask(position);
                    break;
                case TasksRecyclerViewAdapter.MENU_ENTER_CHILD_TASK:
                    updateChildTaskInLongTermTask(position);
                    break;
                case TasksRecyclerViewAdapter.MENU_DELETE_THIS_TASK:
                    longTermTaskList.remove(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case TasksRecyclerViewAdapter.MENU_CHANGE_TASK_STATE:
                    if(longTermTaskList.get(position).isFinished()) longTermTaskList.get(position).setFinished(false);
                    else longTermTaskList.get(position).setFinished(true);
                    tasksRecyclerViewAdapter.notifyItemChanged(position);
                    break;
                case TasksRecyclerViewAdapter.MENU_CUT_THIS_TASK:
                    longTermTaskList.remove(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case TasksRecyclerViewAdapter.MENU_INSERT_A_TASK:
                    insertATask(position);
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }

    private void updateChildTaskInLongTermTask(final int position) {
        if(((TaskActivity) getRunningActivity()).longTermTaskList.get(position).longTermTaskList.size() > 0) {
            Intent intent = new Intent(TaskActivity.this, TaskActivity.class);
            intent.putExtra("parent_position", position);
            intent.putExtra("task_factory_kind", LONGTERMTASK.ordinal());
            intent.putExtra("parent_name", longTermTaskList.get(position).getName());
            intent.putExtra("mode", CHILD_UPDATE_MODE);
            try {
                intent.putExtra("long_term_task_list", serializeforTaskList(longTermTaskList.get(position).longTermTaskList));
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Toast.makeText(getRunningActivity(), "没有子项任务", Toast.LENGTH_SHORT).show();
        }
    }

    private void createChildTaskInLongTermTask(final int position) {
        if(((TaskActivity) getRunningActivity()).longTermTaskList.get(position).longTermTaskList.size() == 0) {
            View view = LayoutInflater.from(TaskActivity.this).inflate(R.layout.temporary_task_dialog_layout, null);
            final TextView longTermTaskTitleTextView = view.findViewById(R.id.temporary_task_title_text_view);
            longTermTaskTitleTextView.setText("设 置 子 任 务");
            final EditText longTermTaskNameEditText = view.findViewById(R.id.temporary_task_name_edit_text);
            final TextView longTermTaskDeadlineEditText = view.findViewById(R.id.temporary_task_deadline_edit_text);
            final Calendar longTermTaskDeadlineCalendar = Calendar.getInstance();
            longTermTaskDeadlineEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePicker datePicker = new DatePicker(TaskActivity.this);
                    datePicker.init(longTermTaskDeadlineCalendar.get(Calendar.YEAR), longTermTaskDeadlineCalendar.get(Calendar.MONTH), longTermTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            longTermTaskDeadlineCalendar.set(year, monthOfYear, dayOfMonth);
                        }
                    });
                    new AlertDialog.Builder(TaskActivity.this)
                            .setView(datePicker)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Calendar nowCalendar = Calendar.getInstance();
                                    if(longTermTaskDeadlineCalendar.before(nowCalendar)){
                                        Toast.makeText(TaskActivity.this, "截止日期无效", Toast.LENGTH_SHORT).show();
                                    } else {
                                        longTermTaskDeadlineEditText.setText(longTermTaskDeadlineCalendar.get(Calendar.YEAR) + "年" + (longTermTaskDeadlineCalendar.get(Calendar.MONTH) + 1) + "月" + longTermTaskDeadlineCalendar.get(Calendar.DAY_OF_MONTH) + "日");
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            final EditText longTermTaskDetailEditText = view.findViewById(R.id.temporary_task_detail_edit_text);
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String longTermTaskName = longTermTaskNameEditText.getText().toString().trim();
                            String longTermTaskDeadline = longTermTaskDeadlineEditText.getText().toString();
                            String longTermTaskDetail = longTermTaskDetailEditText.getText().toString();
                            if(longTermTaskName.equals("") || longTermTaskDeadline.equals("")){
                                Toast.makeText(TaskActivity.this, "任务名或截止日期不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                                LongTermTask longTermTask = new LongTermTask(longTermTaskName, longTermTaskDetail, longTermTaskDeadlineCalendar, TimeUtil.getMinuteCreateTimeCalendar());
                                Intent intent = new Intent(TaskActivity.this, TaskActivity.class);
                                intent.putExtra("parent_position", position);
                                intent.putExtra("task_factory_kind", LONGTERMTASK.ordinal());
                                intent.putExtra("parent_name", longTermTaskList.get(position).getName());
                                intent.putExtra("mode", CHILD_CREATE_MODE);
                                try {
                                    intent.putExtra("long_term_task", serializeforLongtermTask(longTermTask));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                startActivityForResult(intent, REQUEST_CODE);
                            }
                        }
                    }).setNegativeButton("取消", null);
            builder.show();
        } else {
            Toast.makeText(getRunningActivity(), "已有子项任务列表", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertATask(int position){
        SharedPreferences sharedPreferences = getSharedPreferences("copy_cache", MODE_PRIVATE);
        if(sharedPreferences.getInt("task_kind", -1) != -1){
            TaskKind taskKind = TaskKind.values()[sharedPreferences.getInt("task_kind", -1)];
            if(taskKind == this.taskKind) {
                try {
                    switch (taskKind) {
                        case TEMPORARYTASK:
                            TemporaryTask temporaryTask = (TemporaryTask) deSerializeforTask(sharedPreferences.getString("task", ""), this.taskKind);
                            temporaryTaskList.add(position, temporaryTask);
                            return;
                        case PERIODICTASK:
                            PeriodicTask periodicTask = (PeriodicTask) deSerializeforTask(sharedPreferences.getString("task", ""), this.taskKind);
                            periodicTaskList.add(position, periodicTask);
                            return;
                        case LONGTERMTASK:
                            LongTermTask longTermTask = (LongTermTask) deSerializeforTask(sharedPreferences.getString("task", ""), this.taskKind);
                            longTermTaskList.add(position, longTermTask);
                            return;
                        default:
                            return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(TaskActivity.this, "任务类型不一致", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(TaskActivity.this, "未复制或剪切任务", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortTask(){
        if(taskListRecyclerView.getChildCount() > 0){
            TextView sortByTaskNameTextView = new TextView(TaskActivity.this);
            sortByTaskNameTextView.setText("            按任务名称排序");
            sortByTaskNameTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByTaskNameTextView.setTextSize(20);

            TextView sortByCreateTimeTextView = new TextView(TaskActivity.this);
            sortByCreateTimeTextView.setText("            按创建时间排序");
            sortByCreateTimeTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByCreateTimeTextView.setTextSize(20);

            TextView sortByDeadlineTextView = new TextView(TaskActivity.this);
            sortByDeadlineTextView.setText("            按截止日期排序");
            sortByDeadlineTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByDeadlineTextView.setTextSize(20);

            //周期任务
            TextView sortByStartDateTextView = new TextView(TaskActivity.this);
            sortByStartDateTextView.setText("            按开始执行时间排序");
            sortByStartDateTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByStartDateTextView.setTextSize(20);

            //长期任务
            TextView sortByChildCountTextView = new TextView(TaskActivity.this);
            sortByChildCountTextView.setText("            按子任务数量排序");
            sortByChildCountTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByChildCountTextView.setTextSize(20);

            LinearLayout sortLinearLayout = new LinearLayout(TaskActivity.this);
            sortLinearLayout.setOrientation(LinearLayout.VERTICAL);
            sortLinearLayout.addView(sortByTaskNameTextView);
            if(taskKind == TEMPORARYTASK || taskKind == LONGTERMTASK) {
                sortLinearLayout.addView(sortByCreateTimeTextView);
                sortLinearLayout.addView(sortByDeadlineTextView);
            } else if (taskKind == PERIODICTASK) {
                sortLinearLayout.addView(sortByCreateTimeTextView);
                sortLinearLayout.addView(sortByStartDateTextView);
            }
            if(taskKind == LONGTERMTASK) {
                sortLinearLayout.addView(sortByChildCountTextView);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
            final AlertDialog alertDialog = builder.setView(sortLinearLayout).setCancelable(true).show();

            sortByTaskNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskKind == TEMPORARYTASK) {
                        Collections.sort(temporaryTaskList, new Comparator<TemporaryTask>() {
                            @Override
                            public int compare(TemporaryTask temporaryTask, TemporaryTask t1) {
                                if(!sortByTaskNameFlag) {
                                    if(temporaryTask.getName().compareTo(t1.getName()) < 0) return -1;
                                    else if(temporaryTask.getName().compareTo(t1.getName()) > 0) return 1;
                                    return 0;
                                } else {
                                    if(temporaryTask.getName().compareTo(t1.getName()) < 0) return 1;
                                    else if(temporaryTask.getName().compareTo(t1.getName()) > 0) return -1;
                                    return 0;
                                }
                            }
                        });
                    } else if (taskKind == PERIODICTASK) {
                        Collections.sort(periodicTaskList, new Comparator<PeriodicTask>() {
                            @Override
                            public int compare(PeriodicTask periodicTask, PeriodicTask p1) {
                                if(!sortByTaskNameFlag) {
                                    if(periodicTask.getName().compareTo(p1.getName()) < 0) return -1;
                                    else if(periodicTask.getName().compareTo(p1.getName()) > 0) return 1;
                                    return 0;
                                } else {
                                    if(periodicTask.getName().compareTo(p1.getName()) < 0) return 1;
                                    else if(periodicTask.getName().compareTo(p1.getName()) > 0) return -1;
                                    return 0;
                                }
                            }
                        });
                    } else if (taskKind == LONGTERMTASK) {
                        Collections.sort(longTermTaskList, new Comparator<LongTermTask>() {
                            @Override
                            public int compare(LongTermTask longTermTask, LongTermTask l1) {
                                if(!sortByTaskNameFlag) {
                                    if(longTermTask.getName().compareTo(l1.getName()) < 0) return -1;
                                    else if(longTermTask.getName().compareTo(l1.getName()) > 0) return 1;
                                    return 0;
                                } else {
                                    if(longTermTask.getName().compareTo(l1.getName()) < 0) return 1;
                                    else if(longTermTask.getName().compareTo(l1.getName()) > 0) return -1;
                                    return 0;
                                }
                            }
                        });
                    }
                    if(!sortByTaskNameFlag) sortByTaskNameFlag = true;
                    else sortByTaskNameFlag = false;
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            });

            sortByCreateTimeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskKind == TEMPORARYTASK) {
                        Collections.sort(temporaryTaskList, new Comparator<TemporaryTask>() {
                            @Override
                            public int compare(TemporaryTask temporaryTask, TemporaryTask t1) {
                                if(!sortByCreateTimeFlag) {
                                    if(temporaryTask.getCreateTimeCalendar().before(t1.getCreateTimeCalendar())) return -1;
                                    else if(temporaryTask.getCreateTimeCalendar().after(t1.getCreateTimeCalendar())) return 1;
                                    return 0;
                                } else {
                                    if(temporaryTask.getCreateTimeCalendar().before(t1.getCreateTimeCalendar())) return 1;
                                    else if(temporaryTask.getCreateTimeCalendar().after(t1.getCreateTimeCalendar())) return -1;
                                    return 0;
                                }
                            }
                        });
                    } else if (taskKind == PERIODICTASK) {
                        Collections.sort(periodicTaskList, new Comparator<PeriodicTask>() {
                            @Override
                            public int compare(PeriodicTask periodicTask, PeriodicTask p1) {
                                if(!sortByCreateTimeFlag) {
                                    if(periodicTask.getCreateTimeCalendar().before(p1.getCreateTimeCalendar())) return -1;
                                    else if(periodicTask.getCreateTimeCalendar().after(p1.getCreateTimeCalendar())) return 1;
                                    return 0;
                                } else {
                                    if(periodicTask.getCreateTimeCalendar().before(p1.getCreateTimeCalendar())) return 1;
                                    else if(periodicTask.getCreateTimeCalendar().after(p1.getCreateTimeCalendar())) return -1;
                                    return 0;
                                }
                            }
                        });
                    } else if (taskKind == LONGTERMTASK) {
                        Collections.sort(longTermTaskList, new Comparator<LongTermTask>() {
                            @Override
                            public int compare(LongTermTask longTermTask, LongTermTask l1) {
                                if(!sortByCreateTimeFlag) {
                                    if(longTermTask.getCreateTimeCalendar().before(l1.getCreateTimeCalendar())) return -1;
                                    else if(longTermTask.getCreateTimeCalendar().after(l1.getCreateTimeCalendar())) return 1;
                                    return 0;
                                } else {
                                    if(longTermTask.getCreateTimeCalendar().before(l1.getCreateTimeCalendar())) return 1;
                                    else if(longTermTask.getCreateTimeCalendar().after(l1.getCreateTimeCalendar())) return -1;
                                    return 0;
                                }
                            }
                        });
                    }
                    if(!sortByCreateTimeFlag) sortByCreateTimeFlag = true;
                    else sortByCreateTimeFlag = false;
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            });

            sortByDeadlineTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskKind == TEMPORARYTASK) {
                        Collections.sort(temporaryTaskList, new Comparator<TemporaryTask>() {
                            @Override
                            public int compare(TemporaryTask temporaryTask, TemporaryTask t1) {
                                if(!sortByDeadlineFlag) {
                                    if(temporaryTask.getDeadlineCalendar().before(t1.getDeadlineCalendar())) return -1;
                                    else if(temporaryTask.getDeadlineCalendar().after(t1.getDeadlineCalendar())) return 1;
                                    return 0;
                                } else {
                                    if(temporaryTask.getDeadlineCalendar().before(t1.getDeadlineCalendar())) return 1;
                                    else if(temporaryTask.getDeadlineCalendar().after(t1.getDeadlineCalendar())) return -1;
                                    return 0;
                                }
                            }
                        });
                    } else if (taskKind == LONGTERMTASK) {
                        Collections.sort(longTermTaskList, new Comparator<LongTermTask>() {
                            @Override
                            public int compare(LongTermTask longTermTask, LongTermTask l1) {
                                if(!sortByCreateTimeFlag) {
                                    if(longTermTask.getDeadlineCalendar().before(l1.getDeadlineCalendar())) return -1;
                                    else if(longTermTask.getDeadlineCalendar().after(l1.getDeadlineCalendar())) return 1;
                                    return 0;
                                } else {
                                    if(longTermTask.getDeadlineCalendar().before(l1.getDeadlineCalendar())) return 1;
                                    else if(longTermTask.getDeadlineCalendar().after(l1.getDeadlineCalendar())) return -1;
                                    return 0;
                                }
                            }
                        });
                    }
                    if(!sortByDeadlineFlag) sortByDeadlineFlag = true;
                    else sortByDeadlineFlag = false;
                    tasksRecyclerViewAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            });

            sortByStartDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskKind == PERIODICTASK) {
                        Collections.sort(periodicTaskList, new Comparator<PeriodicTask>() {
                            @Override
                            public int compare(PeriodicTask periodicTask, PeriodicTask p1) {
                                if (!sortByStartDateFlag) {
                                    if (periodicTask.getStartDateCalendar().before(p1.getStartDateCalendar()))
                                        return -1;
                                    else if (periodicTask.getStartDateCalendar().after(p1.getStartDateCalendar()))
                                        return 1;
                                    return 0;
                                } else {
                                    if (periodicTask.getStartDateCalendar().before(p1.getStartDateCalendar()))
                                        return 1;
                                    else if (periodicTask.getStartDateCalendar().after(p1.getStartDateCalendar()))
                                        return -1;
                                    return 0;
                                }
                            }
                        });
                        if (!sortByStartDateFlag) sortByStartDateFlag = true;
                        else sortByStartDateFlag = false;
                        tasksRecyclerViewAdapter.notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                }
            });

            sortByChildCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskKind == LONGTERMTASK) {
                        Collections.sort(longTermTaskList, new Comparator<LongTermTask>() {
                            @Override
                            public int compare(LongTermTask longTermTask, LongTermTask l1) {
                                if (!sortByChildCountFlag) {
                                    if (longTermTask.longTermTaskList.size() < l1.longTermTaskList.size())
                                        return -1;
                                    else if (longTermTask.longTermTaskList.size() > l1.longTermTaskList.size())
                                        return 1;
                                    return 0;
                                } else {
                                    if (longTermTask.longTermTaskList.size() < l1.longTermTaskList.size())
                                        return 1;
                                    else if (longTermTask.longTermTaskList.size() > l1.longTermTaskList.size())
                                        return -1;
                                    return 0;
                                }
                            }
                        });
                        if (!sortByChildCountFlag) sortByChildCountFlag = true;
                        else sortByChildCountFlag = false;
                        tasksRecyclerViewAdapter.notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                }
            });
        }
        else Toast.makeText(TaskActivity.this ,"请先添加任务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TaskActivity.REQUEST_CODE && resultCode == RESULT_FIRST_USER && data != null) {
            int parentPosition = data.getIntExtra("parent_task_position", -1);
            try {
                longTermTaskList.get(parentPosition).longTermTaskList = (List<LongTermTask>) deSerializeforTaskList(data.getStringExtra("child_task"), LONGTERMTASK);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            tasksRecyclerViewAdapter.notifyDataSetChanged();
        }
    }
}
