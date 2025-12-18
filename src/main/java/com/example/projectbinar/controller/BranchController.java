package com.example.projectbinar.controller;

import com.example.projectbinar.entity.Branch;
import com.example.projectbinar.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branch")
public class BranchController {
    @Autowired
    private BranchService branchService;

    @GetMapping
    public List<Branch> getAllBranches(){ return branchService.getAllBranches(); }

    @PostMapping
    public Branch createBranch(@RequestBody Branch branch){ return branchService.createBranch(branch);}
}
