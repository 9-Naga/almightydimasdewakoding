package com.example.projectbinar.service;

import com.example.projectbinar.entity.Branch;
import com.example.projectbinar.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;

    public Branch createBranch(Branch branch){
        return branchRepository.save(branch);
    }
    public List<Branch> getAllBranches(){ return branchRepository.findAll();
    }
}
