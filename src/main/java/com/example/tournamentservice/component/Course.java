package com.example.tournamentservice.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Getter
public class Course {
    @Value("${course.pars}")
    private List<Integer> pars;
}
