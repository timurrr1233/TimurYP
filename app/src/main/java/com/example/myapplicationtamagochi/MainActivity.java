package com.example.myapplicationtamagochi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView day;
    Button eat, sleep, play, fun;
    Handler handler;
    int setHunger = 5, setFatigue = 5, setBoredom = 5, setHappiness = 5;
    AnimationDrawable mAnimationDrawable;
    ImageView imageView;
    int dayTime = 0;
    int seconds = 0;
    ProgressBar showHunger;
    ProgressBar showFatigue;
    ProgressBar showBoredom;
    ProgressBar showHappiness;
    Drawable drawableHunger, drawableFatigue, drawableBoredom, drawableHappiness;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        imageView = findViewById(R.id.imageView);
        eat = findViewById(R.id.fixHunger);
        sleep = findViewById(R.id.fixFatigue);
        play = findViewById(R.id.fixBoredom);
        fun = findViewById(R.id.fixHappiness);
        drawableHunger = ContextCompat.getDrawable(this, R.drawable.custom_progressbar_hunger);
        drawableFatigue = ContextCompat.getDrawable(this, R.drawable.custom_progressbar_fatigue);
        drawableBoredom = ContextCompat.getDrawable(this, R.drawable.custom_progressbar_boredom);
        drawableHappiness = ContextCompat.getDrawable(this, R.drawable.custom_progressbar_happiness);
        showHunger = findViewById(R.id.progressBarHunger);
        showHunger.setProgressDrawable(drawableHunger);

        showFatigue = findViewById(R.id.progressBarFatigue);
        showFatigue.setProgressDrawable(drawableFatigue);

        showBoredom = findViewById(R.id.progressBarBoredom);
        showBoredom.setProgressDrawable(drawableBoredom);

        showHappiness = findViewById(R.id.progressBarHappiness);
        showHappiness.setProgressDrawable(drawableHappiness);
        updateStatus();
        updateLevelGame();
        //описание действий кнопки "ПОКОРМИТЬ"
        eat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tamagochi.hunger += setHunger;
                Tamagochi.fatigue -= setFatigue;
                Tamagochi.boredom -= setBoredom;
                updateLevelGame();
                handleTamagochiDeath();
                updateStatus();
                checkAlive();
            }
        });
        //описание действий кнопки "ПОСПАТЬ"
        sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tamagochi.hunger -= setHunger;
                Tamagochi.fatigue += setFatigue;
                Tamagochi.boredom -= setBoredom;
                Tamagochi.happiness -= setHappiness;
                updateLevelGame();
                handleTamagochiDeath();
                updateStatus();
                checkAlive();
            }
        });
        //описание действий кнопки "ПОИГРАТЬ"
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tamagochi.hunger -= setHunger;
                Tamagochi.fatigue -= setFatigue;
                Tamagochi.boredom += setBoredom;
                updateLevelGame();
                handleTamagochiDeath();
                updateStatus();
                checkAlive();
            }
        });
        //описание действий кнопки "РАЗВЕСЕЛИТЬ"
        fun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tamagochi.hunger -= setHunger;
                Tamagochi.fatigue += setFatigue;
                Tamagochi.boredom += setBoredom;
                Tamagochi.happiness += setHappiness;
                updateLevelGame();
                handleTamagochiDeath();
                updateStatus();
                checkAlive();
            }
        });

        // метод или же поток жизни тамагочи, увеличивает кол-во прожитых дней, в зависимости от переменной dayTime, которая устанавливается
        // в методе startTimer который ниже, handler.post(() -> отправляет в мейн поток обновленные методы updateStatus, changeAnimationAndMessageByStatus, updateLevelGame
        Thread life = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Tamagochi.alive) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (checkAlive()) {
                                Tamagochi.day++;
                            } else {
                                Tamagochi.alive = false;
                            }
                        }
                    });
                    handler.post(() -> {
                        updateStatus();
                        changeAnimationAndMessageByStatus();
                        updateLevelGame();
                    });
                    try {
                        Thread.sleep(dayTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        life.start();
        startTimer();
    }

    // метод Таймер, считает секунды жизни тамагочи, с каждым проходом, отнимает по одному поинту в прогресс бараз состояний тамагочи
    private void startTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        seconds++;
                        if (checkAlive()) {
                            Tamagochi.hunger -= 1;
                            Tamagochi.fatigue -= 1;
                            Tamagochi.boredom -= 1;
                            Tamagochi.happiness -= 1;
                            updateStatus();
                        } else {
                            eat.setClickable(false);
                            sleep.setClickable(false);
                            play.setClickable(false);
                            fun.setClickable(false);
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    // если тамагочи живет меньше или 5 дней, тогда длительность дня равно 5 секундам, когда же кол-во дней становится больше 5
    // длительность дня становится равна 3 секундам и с каждой секундой начинается вычитание по 10 поинтов в прогресс бараз состояний тамагочи
    public void updateLevelGame() {
        if (Tamagochi.day <= 5) {
            dayTime = 5000;
        }
        if (Tamagochi.day > 5) {
            dayTime = 3000;
            setHunger = 10;
            setFatigue = 10;
            setBoredom = 10;
            setHappiness = 10;
        }
    }

    // проверяет живой ли тамагочи или нет, если любой показатель равен 0, тогда возврачаешь false и вызывается в методе с потоком жизни
    public boolean checkAlive() {
        if (Tamagochi.hunger == 0 || Tamagochi.boredom == 0 || Tamagochi.fatigue == 0 || Tamagochi.happiness == 0) {
            updateStatus();
            return false;
        }
        return true;
    }

    // метод обновления данных в прогресс барах, вызывается в методах где значение либо увеличивается, либо уменьшается
    @SuppressLint("SetTextI18n")
    public void updateStatus() {
        showHunger.setProgress(Tamagochi.hunger);
        showFatigue.setProgress(Tamagochi.fatigue);
        showBoredom.setProgress(Tamagochi.boredom);
        showHappiness.setProgress(Tamagochi.happiness);
    }

    // смена анимации в зависимости от статуса показателей
    public void changeAnimationAndMessageByStatus() {
        // если все показатели больше 50, тогда он веселый капец
        if (Tamagochi.hunger >= 50 || Tamagochi.fatigue >= 50 ||
                Tamagochi.boredom >= 50 || Tamagochi.happiness >= 50) {
            imageView.setBackgroundResource(R.drawable.allprocentanimation);
            mAnimationDrawable = (AnimationDrawable) imageView.getBackground();
            mAnimationDrawable.start();
        }
        // если любой показатель меньше 50, тогда он злой
        if (Tamagochi.hunger < 50 || Tamagochi.fatigue < 50 ||
                Tamagochi.boredom < 50 || Tamagochi.happiness < 50){
            imageView.setBackgroundResource(R.drawable.arg);
            Toast.makeText(MainActivity.this, "СДЕЛАЙ МЕНЯ СЧАСТЛИВЫМ, ИНАЧЕ Я ОПУХНУ", Toast.LENGTH_SHORT).show();
        }
        // если любой показатель равен 0, тогда -> умер мужик
        if (Tamagochi.hunger == 0 || Tamagochi.fatigue == 0 ||
                Tamagochi.boredom == 0 || Tamagochi.happiness == 0) {
            imageView.setBackgroundResource(R.drawable.dead);
        }
    }
    // если любой показатель равен или меньше 0, тогда -> умер мужик
    // все показатели становятся равны 0, выводится уведомление снизу о том что он умер и стартует новая активность
    private void handleTamagochiDeath() {
        if (Tamagochi.hunger <= 0 || Tamagochi.fatigue <= 0 || Tamagochi.boredom <= 0 || Tamagochi.happiness <= 0) {
            Tamagochi.hunger = 0;
            Tamagochi.fatigue = 0;
            Tamagochi.boredom = 0;
            Tamagochi.happiness = 0;
            Toast.makeText(MainActivity.this, "Хазбумочи умер! Жил он ровно: " + Tamagochi.day + " дней", Toast.LENGTH_SHORT).show();
            Tamagochi.alive = false;
            Intent intent = new Intent(MainActivity.this, DeadActivity.class);
            intent.putExtra("TIME", seconds);
            startActivity(intent);
            mAnimationDrawable.stop();
            imageView.setBackgroundResource(R.drawable.dead);
        }
    }
}