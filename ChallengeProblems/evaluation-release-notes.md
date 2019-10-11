# Evaluation Release Notes

This document is intended to provide release notes and useful tips for evaluation.

## Scenario 5

### Known Issues

 - A Heisenbug has unfortunately been found shortly before the code freeze where no valid solution was found. It worked from the staging branch, failed once on the master branch, then succeeded with another build. Everything is built to be deterministic

### Evaluation Guidance

#### Dau Selection Limiting

By default, a maximum of two DAUs will be selected to replace the faulty Dau(s) within a Flight Test configuration. This is in place to prevent a search space explosion. It however can be overridden  by providing a `--max-dau-result-count` parameter followed by an integer to the `start.sh` script as follows:

`./start.sh --scenario 5 --max-dau-result-count 4`

#### Avoiding Search Space Explosion

Since the DSL is not using a simple greedy algorithm that could exhaust flexible resources with more complex configurations, grouping is used internally to minimize the search space. However, if grouping cannot be easily done, the duration of the search can increase exponentially.

The primary thing we have seen that can cause search space explosion is higher degrees of non-uniformity of Measurements among all measurements in the search space that satisfy requirements.

So for example, say you have the following requirements for 6 ports in the MDLRoot:
```
"Port" {
    "Measurement": {
        "SampleRate": {
            "Min": 60,
            "Max": 384
        },
        "DataLength": {
            "Min": 12,
            "Max": 16
        },
        "DataRate": {
            "Min": 0,
            "Max": 32768
        }
    }
}
```

Since Dau and Module separation seems to have minimal impact at this point since we are optimizing on cost, say the inventory Ports look like this:


```
"PortA": {
    "Measurement": [
        {
            "SampleRate": 128,
            "DataLength": 16
        },
        {
            "SampleRate": 192,
            "DataLength": 12
        }
    ]
},
"PortB": {
    "Measurement": [
        {
            "SampleRate": 128,
            "DataLength": 16
        },
        {
            "SampleRate": 192,
            "DataLength": 12
        }
    ]
},
"PortC": {
    "Measurement": [
        {
            "SampleRate": 128,
            "DataLength": 16
        },
        {
            "SampleRate": 192,
            "DataLength": 12
        }
    ]
},
"PortD": {
    "Measurement": [
        {
            "SampleRate": 128,
            "DataLength": 16
        },
        {
            "SampleRate": 190,
            "DataLength": 12
        },
        {
            "SampleRate": 192,
            "DataLength": 12

        }
    ]
},
"PortE": {
    "Measurement": [
        {
            "SampleRate": 128,
            "DataLength": 17
        },
        {
            "SampleRate": 192,
            "DataLength": 11
        }
    ]
},
"PortF": {
    "Measurement": [
        {
            "SampleRate": 128,
            "DataLength": 17
        },
        {
            "SampleRate": 9999,
            "DataLength": 96
        }
        {
            "SampleRate": 192,
            "DataLength": 11
        }
    ]
}
```

In this scenario we have the following:

| Port  | Matches   | Grouping                          | 
|-------|:---------:|-----------------------------------|
| PortA | Yes       | SR128DL16orSR192DL12              |
| PortB | Yes       | SR128DL16orSR192DL12              |
| PortC | Yes       | SR128DL16orSR192DL12              |
| PortD | Yes       | SR128DL16orSR190DL12orSR192DL12   |
| PortE | No        | SR128DL17orSR192DL11              |
| PortF | No        | SR128DL17orSR999DL96orSR192DL11   |

PortD breaking the uniformity of PortA, PortB, and PortC causes the search space to increase since it is valid but must be examined individually to ensure the overall constraints on the entire problem are satisfied.  However, PortE and PortF have no impact since they do not satisfy the valid ranges. 

Similarly, if PortD had a non-matching PortType, BBNPortFunctionality, Direction, Excitation configuration, or PortPolarity, it would be omitted earlier, and the explosion would be averted.