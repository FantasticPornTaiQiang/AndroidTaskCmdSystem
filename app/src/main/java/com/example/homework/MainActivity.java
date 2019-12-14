package com.example.homework;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homework.listener.OnItemClickListener;
import com.example.homework.adapter.TaskFactoryListRecyclerViewAdapter;
import com.example.homework.data.TaskFactory;
import com.example.homework.data.TaskKind;
import com.example.homework.listener.OnItemTouchListener;
import com.example.homework.utils.MyItemTouchHelper;
import com.example.homework.utils.PermissionUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.homework.utils.SerializeAndDeserialize.deSerializeforTaskFactoryList;
import static com.example.homework.utils.SerializeAndDeserialize.deSerializeforTaskList;
import static com.example.homework.utils.SerializeAndDeserialize.serializeforTaskFactoryList;
import static com.example.homework.utils.SerializeAndDeserialize.serializeforTaskList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    public static final int CREATE_MODE = 0;//点击添加进入Task界面
    public static final int UPDATE_MODE = 1;//点击列表项进入Task界面
    public static final int CHILD_CREATE_MODE = 2;
    public static final int CHILD_UPDATE_MODE = 3;

    private int chooseTaskKindIndex;
    private boolean isChooseTaskKind = false;
    private boolean sortByNameFlag = false;
    private boolean sortByKindFlag = false;

    TextView addTaskFactoryButton;
    TextView taskFactoryMenuButton;

    RecyclerView taskFactoryRecyclerView;
    TaskFactoryListRecyclerViewAdapter taskFactoryListRecyclerViewAdapter;

    private List<TaskFactory> taskFactoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEvent();
        initView();
    }

    private void initEvent() {
        SharedPreferences sharedPreferences = getSharedPreferences("all_task_factory_data", MODE_PRIVATE);
        try {
            taskFactoryList = deSerializeforTaskFactoryList(sharedPreferences.getString("all_data", ""));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveAll() {
        SharedPreferences.Editor editor = getSharedPreferences("all_task_factory_data", MODE_PRIVATE).edit();
        try {
            editor.putString("all_data", serializeforTaskFactoryList(taskFactoryList));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            editor.apply();
        }
    }

    private void sortTaskFactory() {
        if(taskFactoryRecyclerView.getChildCount() > 0){
            TextView sortByNameTextView = new TextView(MainActivity.this);
            sortByNameTextView.setText("            按清单名称排序");
            sortByNameTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByNameTextView.setTextSize(20);

            TextView sortByKindTextView = new TextView(MainActivity.this);
            sortByKindTextView.setText("            按清单类型排序");
            sortByKindTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sortByKindTextView.setTextSize(20);

            LinearLayout sortLinearLayout = new LinearLayout(MainActivity.this);
            sortLinearLayout.setOrientation(LinearLayout.VERTICAL);
                sortLinearLayout.addView(sortByNameTextView);
                sortLinearLayout.addView(sortByKindTextView);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            final AlertDialog alertDialog = builder.setView(sortLinearLayout).setCancelable(true).show();

            sortByNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Collections.sort(taskFactoryList, new Comparator<TaskFactory>() {
                        @Override
                        public int compare(TaskFactory taskFactory, TaskFactory t1) {
                            if(!sortByNameFlag) {
                                if(taskFactory.getName().compareTo(t1.getName()) <= 0) return -1;
                                else if(taskFactory.getName().compareTo(t1.getName()) > 0) return 1;
                                return 0;
                            } else {
                                if(taskFactory.getName().compareTo(t1.getName()) <= 0) return 1;
                                else if(taskFactory.getName().compareTo(t1.getName()) > 0) return -1;
                                return 0;
                            }
                        }
                    });
                    if(!sortByNameFlag) sortByNameFlag = true;
                    else sortByNameFlag = false;
                    taskFactoryListRecyclerViewAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            });

            sortByKindTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Collections.sort(taskFactoryList, new Comparator<TaskFactory>() {
                        @Override
                        public int compare(TaskFactory taskFactory, TaskFactory t1) {
                            if(!sortByKindFlag) {
                                if(taskFactory.taskKind.ordinal() < t1.taskKind.ordinal()) return -1;
                                else if(taskFactory.taskKind.ordinal() > t1.taskKind.ordinal()) return 1;
                                return 0;
                            } else {
                                if(taskFactory.taskKind.ordinal() < t1.taskKind.ordinal()) return 1;
                                else if(taskFactory.taskKind.ordinal() > t1.taskKind.ordinal()) return -1;
                                return 0;
                            }
                        }
                    });
                    if(!sortByKindFlag) sortByKindFlag = true;
                    else sortByKindFlag = false;
                    taskFactoryListRecyclerViewAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            });
        }
        else Toast.makeText(MainActivity.this ,"请先添加任务", Toast.LENGTH_SHORT).show();
    }

    private void exportTaskFactory() {
        final EditText editText = new EditText(MainActivity.this);
        TextView textView = new TextView(MainActivity.this);
        textView.setText("保存的文件在SD卡根目录下");
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        editText.setHint("请输入导出的文件名");
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setTitle("导出为文件")
                .setView(linearLayout)
                .setPositiveButton("导出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fileName = editText.getText().toString().trim().toLowerCase();
                        if(fileName.equals("")) {
                            Toast.makeText(MainActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
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
                            PermissionUtil.verifyStoragePermissions(MainActivity.this);
                            try {
                                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();//获取sd卡路径
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(sdPath + "/" + fileName + ".txt",true));//sd卡
                                bufferedOutputStream.write(serializeforTaskFactoryList(taskFactoryList).getBytes());
                                bufferedOutputStream.close();
                                Toast.makeText(MainActivity.this, "导出成功", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, "导出失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消", null);
        builder.show();
    }

    private void importTaskFactory() {
        final EditText editText = new EditText(MainActivity.this);
        TextView textView = new TextView(MainActivity.this);
        textView.setText("从SD卡根目录下导入文件");
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        editText.setHint("请输入导入的文件名(带完整后缀)");
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setTitle("导入文件")
                .setView(linearLayout)
                .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fileName = editText.getText().toString().trim().toLowerCase();
                        if(fileName.equals("")) {
                            Toast.makeText(MainActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else {
                            PermissionUtil.verifyStoragePermissions(MainActivity.this);
                            try {
                                File file = new File(Environment.getExternalStorageDirectory(), fileName);
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                                String readline;
                                StringBuilder stringBuilder = new StringBuilder();
                                while ((readline = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(readline);
                                }
                                bufferedReader.close();
                                taskFactoryList.addAll(deSerializeforTaskFactoryList(stringBuilder.toString()));
                                taskFactoryListRecyclerViewAdapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "导入失败  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消", null);
        builder.show();
    }

    private void initView() {
        taskFactoryMenuButton = findViewById(R.id.task_factory_menu_button);
        taskFactoryMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.task_factory_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.save_all_change_button:
                                saveAll();
                                return true;
                            case R.id.sort_task_list_button:
                                sortTaskFactory();
                                return true;
                            case R.id.import_task_list_button:
                                importTaskFactory();
                                return true;
                            case R.id.export_task_list_button:
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

        addTaskFactoryButton = findViewById(R.id.add_task_factory_button);
        addTaskFactoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] taskFactoryChoices = {"临时任务", "周期任务", "长期任务"};
                final EditText editText = new EditText(MainActivity.this);
                editText.setSingleLine(true);
                editText.setHint("请输入任务清单名");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("添加任务清单")
                        .setCancelable(false)
                        .setView(editText)
                        .setSingleChoiceItems(taskFactoryChoices, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                isChooseTaskKind = true;
                                chooseTaskKindIndex = i;
                            }
                        })
                        .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String taskFactoryName = editText.getText().toString().trim();
                                if(taskFactoryName.equals("")){
                                    Toast.makeText(MainActivity.this, "名字不能为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                                    intent.putExtra("task_factory_name", taskFactoryName);
                                    if(!isChooseTaskKind) chooseTaskKindIndex = 0;
                                    isChooseTaskKind = false;
                                    intent.putExtra("task_factory_kind", chooseTaskKindIndex);
                                    intent.putExtra("mode", CREATE_MODE);
                                    startActivityForResult(intent, REQUEST_CODE);
                                }
                            }
                        }).setNegativeButton("取消", null);
                builder.show();
            }
        });

        taskFactoryRecyclerView = findViewById(R.id.task_factory_recycler_view);
        taskFactoryListRecyclerViewAdapter = new TaskFactoryListRecyclerViewAdapter(taskFactoryList);
        //设置布局管理器
        taskFactoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置Adapter
        taskFactoryRecyclerView.setAdapter(taskFactoryListRecyclerViewAdapter);

        taskFactoryListRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                intent.putExtra("mode", UPDATE_MODE);
                intent.putExtra("position", position);
                intent.putExtra("task_factory_name", taskFactoryList.get(position).getName());
                intent.putExtra("task_factory_kind", taskFactoryList.get(position).taskKind.ordinal());
                try {
                    intent.putExtra("task_list", serializeforTaskList(taskFactoryList.get(position).taskList));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        MyItemTouchHelper touchHelper = new MyItemTouchHelper(new OnItemTouchListener() {
            //拖动排序
            @Override
            public boolean onMove(int fromPosition, int toPosition) {
                if (fromPosition < toPosition) {
                    //从上往下拖动，每滑动一个item，都将list中的item向下交换，向上滑同理。
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(taskFactoryList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(taskFactoryList, i, i - 1);
                    }
                }
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //notifyItemMoved只是告诉RecyclerView它的Item换了位置，也就是说如果没有它那么你只能拖动
                //某一项而不能让它与其他项互换位置但是notifyItemMoved仅仅是互换了界面上的项，你必须还要
                //通知RecyclerView它的内容也改变了，这就要用notifyItemRangeChanged
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                taskFactoryListRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition);
                taskFactoryListRecyclerViewAdapter.notifyItemRangeChanged(Math.min(fromPosition, toPosition),
                        Math.abs(fromPosition - toPosition) +1);
                return true;
            }
            //滑动删除
            @Override
            public void onSwiped(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false)
                        .setTitle("确认删除该任务清单吗")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                taskFactoryList.remove(position);
                                taskFactoryListRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                taskFactoryListRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .show();
            }
        });
        touchHelper.setSort(true);//打开拖动排序
        touchHelper.setDelete(true);//打开滑动删除
        new ItemTouchHelper(touchHelper).attachToRecyclerView(taskFactoryRecyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && requestCode == REQUEST_CODE && resultCode == RESULT_FIRST_USER && data.getIntExtra("task_kind", -1) >= 0) {
            if(data.getIntExtra("operate_mode", -1) == CREATE_MODE) {
                TaskKind taskKind = TaskKind.values()[data.getIntExtra("task_kind", -1)];
                String taskFactoryName = data.getStringExtra("task_list_name");
                TaskFactory taskFactory = new TaskFactory(taskFactoryName, taskKind);
                try {
                    taskFactory.taskList = deSerializeforTaskList(data.getStringExtra("task_list"), taskKind);
                    taskFactoryList.add(taskFactory);
                    taskFactoryListRecyclerViewAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (data.getIntExtra("operate_mode", -1) == UPDATE_MODE) {
                int position = data.getIntExtra("position", -1);
                try {
                    taskFactoryList.get(position).taskList = deSerializeforTaskList(data.getStringExtra("task_list"), taskFactoryList.get(position).taskKind);
                    taskFactoryListRecyclerViewAdapter.notifyItemChanged(position);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
